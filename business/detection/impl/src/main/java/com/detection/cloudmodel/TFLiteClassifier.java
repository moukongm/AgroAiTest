package com.detection.cloudmodel;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import com.common.utils.LogUtils;
import com.detection.cloudmodel.Recognition;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TFLiteClassifier {
    //模型
    private Interpreter tflite;
    //标签
    private List<String> labels;
    //输入尺寸
    private int inputSize;
    //模型训练标签数量
    private int numClasses;

    // 模型加载
    public TFLiteClassifier(InputStream modelStream, InputStream labelStream) throws IOException {
        // 加载模型文件到 ByteBuffer
        ByteBuffer modelBuffer = loadModelFile(modelStream);
        
        // 创建推理解释器
        tflite = new Interpreter(modelBuffer);

        // 通过解释器的第一个子项获取它的shape形状，得到一个数组，根据数组的大小可以知道有几个维度（高度，宽度这种）
        try {
            int[] inputShape = tflite.getInputTensor(0).shape();
            // 更新 inputSize 为模型实际要求的尺寸
            if (inputShape.length >= 2) {
                inputSize = inputShape[1]; // 假设第二维是高度
            }
        } catch (Exception e) {
            LogUtils.INSTANCE.e("TFLite", "获取输入张量信息失败", e);
        }

        labels = loadLabels(labelStream);
        numClasses = labels.size();
    }
    public int getLabelsCount() {
        return labels != null ? labels.size() : 0;
    }

    public int getInputSize() {
        return inputSize;
    }

    //初始化，从文件读到这里；
   // 通过scanner加载标签，txt文件，一行一行读；
    private List<String> loadLabels(InputStream labelStream) throws IOException {
        List<String> result = new ArrayList<>();
        try (Scanner scanner = new Scanner(labelStream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                result.add(line);
            }
        }
        return result;
    }

    //把context.getAssets().open("model.tflite")）文件流存到直接缓冲区并且返回缓冲区，解释器需要缓冲区；
    private ByteBuffer loadModelFile(InputStream modelStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(modelStream);
        //获取可以自动扩容的ByteArrayOutputStream，因为要存完整的模型文件
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] modelBytes = baos.toByteArray();
        
        // 创建直接缓冲区并复制数据，如果你存到堆内缓冲区，最后也是会复制到直接缓冲区的。
        ByteBuffer result = ByteBuffer.allocateDirect(modelBytes.length);
        //把模型放进去
        result.put(modelBytes);
        //翻转，不然指针在最后，你怎么读文件
        result.flip();
        return result;
    }
    // 执行推理，模型要求是bytebuffer
    public List<Recognition> recognizeImage(ByteBuffer inputBuffer) {
        // 确保是直接缓冲区
        if (!inputBuffer.isDirect()) {
            throw new IllegalArgumentException("Input buffer must be a direct buffer");
        }
        //确保缓冲区容量负荷模型要求
        if (inputBuffer.capacity() != 1 * inputSize * inputSize * 3 * 4) {
            throw new IllegalArgumentException("Invalid buffer capacity: got " + inputBuffer.capacity() + 
                                               ", expected " + (1 * inputSize * inputSize * 3 * 4));
        }

        //把结果写到output里返回；
        //一维代表图片，二维代表概率，这里只识别一张图，所以是output[0]；
        float[][] output = new float[1][numClasses];
        tflite.run(inputBuffer, output);
        return getRecognitions(output[0]);
    }

    //因为本地和上网不一样，所以用的Model也不一样；
    //这里的大概逻辑是维护一个小顶堆找到top3；
    //概率太小的不显示；
    //根据labels.get(idx);获取名字，转换中文，获取解决方案；创建新类，返回回去；
    private List<Recognition> getRecognitions(float[] probabilities) {
        List<Recognition> recognitions = new ArrayList<>();

        // 低于此值的结果不显示
        final float MIN_CONFIDENCE_THRESHOLD = 15.0f; // 15%
        int topK = 3;

        // 使用优先队列找出 topK，维护一个topk数量的队列按照从小到大排序小顶堆；
        PriorityQueue<Integer> pq = new PriorityQueue<>(topK,
                (a, b) -> Float.compare(probabilities[a], probabilities[b]));

        //如果数量大了移除队首，所以最后能找到topk个最大的；
        for (int i = 0; i < probabilities.length; i++) {
            pq.offer(i);
            if (pq.size() > topK) {
                pq.poll();
            }
        }

        List<Integer> topIndices = new ArrayList<>(pq);
        Collections.reverse(topIndices);

        // 构建识别结果，过滤低置信度
        for (int idx : topIndices) {
            float confidence = probabilities[idx] * 100f;  // 转换为百分比
            // 只添加置信度高于阈值的结果
            if (confidence < MIN_CONFIDENCE_THRESHOLD) {
                continue;
            }
            String diseaseName = labels.get(idx);
            String chineseName = getChineseName(diseaseName);  // 转换为中文名
            String controlPlan = getControlPlan(diseaseName);  // 获取防治方案

            recognitions.add(new Recognition(chineseName, confidence, controlPlan));
        }

        LogUtils.INSTANCE.d("TFLite", "最终结果数量: " + recognitions.size());
        return recognitions;
    }
    
    /**
     * 病害名称英文转中文
     */
    private String getChineseName(String englishName) {
        switch (englishName) {
            // 苹果
            case "apple apple scab": return "苹果黑星病";
            case "apple black rot": return "苹果黑腐病";
            case "apple cedar apple rust": return "苹果锈病";
            case "apple healthy": return "苹果健康";
            
            // 蓝莓
            case "blueberry healthy": return "蓝莓健康";
            
            // 樱桃
            case "cherry including sour powdery mildew": return "樱桃白粉病";
            case "cherry including sour healthy": return "樱桃健康";
            
            // 玉米
            case "corn maize cercospora leaf spot gray leaf spot": return "玉米灰斑病";
            case "corn maize common rust": return "玉米普通锈病";
            case "corn maize northern leaf blight": return "玉米大斑病";
            case "corn maize healthy": return "玉米健康";
            
            // 葡萄
            case "grape black rot": return "葡萄黑腐病";
            case "grape esca black measles": return "葡萄卷叶病";
            case "grape leaf blight isariopsis leaf spot": return "葡萄叶斑病";
            case "grape healthy": return "葡萄健康";
            
            // 柑橘
            case "orange haunglongbing citrus greening": return "柑橘黄龙病";
            
            // 桃子
            case "peach bacterial spot": return "桃细菌性斑点病";
            case "peach healthy": return "桃树健康";
            
            // 辣椒
            case "pepper bell bacterial spot": return "辣椒细菌性斑点病";
            case "pepper bell healthy": return "辣椒健康";
            
            // 马铃薯
            case "potato early blight": return "马铃薯早疫病";
            case "potato late blight": return "马铃薯晚疫病";
            case "potato healthy": return "马铃薯健康";
            
            // 覆盆子
            case "raspberry healthy": return "覆盆子健康";
            
            // 大豆
            case "soybean healthy": return "大豆健康";
            
            // 南瓜
            case "squash powdery mildew": return "南瓜白粉病";
            
            // 草莓
            case "strawberry leaf scorch": return "草莓叶焦病";
            case "strawberry healthy": return "草莓健康";
            
            // 番茄
            case "tomato bacterial spot": return "番茄细菌性斑点病";
            case "tomato early blight": return "番茄早疫病";
            case "tomato late blight": return "番茄晚疫病";
            case "tomato leaf mold": return "番茄叶霉病";
            case "tomato septoria leaf spot": return "番茄斑枯病";
            case "tomato spider mites two spotted spider mite": return "番茄红蜘蛛";
            case "tomato target spot": return "番茄靶斑病";
            case "tomato tomato yellow leaf curl virus": return "番茄黄化曲叶病毒";
            case "tomato tomato mosaic virus": return "番茄花叶病毒";
            case "tomato healthy": return "番茄健康";
            
            // 背景
            case "background": return "背景";
            
            default: return englishName;
        }
    }

    /**
     * 根据病害名称获取防治方案
     * @param diseaseName 病害名称（与 labels.txt 完全一致）
     * @return 防治方案或健康维护建议
     */
    private String getControlPlan(String diseaseName) {
        // 根据 labels.txt 中的 38 个类别返回具体方案
        switch (diseaseName) {
            // ============ 苹果相关病害 ============
            case "apple apple scab":
                return "1. 农业防治：冬季清园，清除病叶、病果，减少越冬菌源。\n" +
                       "2. 化学防治：萌芽前喷3-5度石硫合剂；发病初期喷洒40%氟硅唑8000倍液或10%苯醚甲环唑1500倍液，间隔10-15天喷一次，连续2-3次。\n" +
                       "3. 预防措施：选用抗病品种，合理修剪保持通风，避免连作。";

            case "apple black rot":
                return "1. 农业防治：清除病枝、病叶、病果，带出园外深埋；冬季喷石硫合剂消毒。\n" +
                       "2. 化学防治：发病初期喷洒70%甲基硫菌灵800倍液或80%代森锰锌600倍液，间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：选用抗病砧木，避免伤口，加强树体营养。";

            case "apple cedar apple rust":
                return "1. 农业防治：清除周围桧柏植物（转主寄主），或在其上喷药防治；冬季修剪病枝。\n" +
                       "2. 化学防治：苹果萌芽期喷20%三唑酮1500倍液或25%戊唑醇2000倍液，间隔10-15天喷一次。\n" +
                       "3. 预防措施：选用抗病品种，避免在桧柏附近建园。";

            case "apple healthy":
                return "苹果叶片健康状况良好，未发现病害症状。\n" +
                       "维护建议：继续保持良好的果园管理，定期巡查，做好冬季清园和病虫害预防工作。";

            // ============ 蓝莓 ============
            case "blueberry healthy":
                return "蓝莓植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：保持土壤酸性（pH 4.5-5.5），合理施肥，注意防虫（如果蝇）。";

            // ============ 樱桃相关病害 ============
            case "cherry including sour powdery mildew":
                return "1. 农业防治：冬季清园，剪除病枝，集中销毁；合理修剪保持通风。\n" +
                       "2. 化学防治：发病初期喷洒15%三唑酮1500倍液或40%氟硅唑8000倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：选用抗病品种，避免过度氮肥，注意排水。";

            case "cherry including sour healthy":
                return "樱桃植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意花期防霜冻，合理修剪，做好食心虫等害虫防治。";

            // ============ 玉米病害 ============
            case "corn maize cercospora leaf spot gray leaf spot":
                return "1. 农业防治：选用抗病品种，清除病残体，轮作倒茬，合理密植。\n" +
                       "2. 化学防治：发病初期喷洒75%肟菌·戊唑醇3000倍液或40%氟硅唑8000倍液，间隔7-10天喷一次，连续2次。\n" +
                       "3. 预防措施：种子包衣处理，增施磷钾肥，提高抗病力。";

            case "corn maize common rust":
                return "1. 农业防治：选用抗病品种，早春清除田间病残体，减少菌源。\n" +
                       "2. 化学防治：发病初期喷洒20%三唑酮1500倍液或25%戊唑醇2000倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：适期播种，避免过早，加强田间管理。";

            case "corn maize northern leaf blight":
                return "1. 农业防治：选用抗病品种，秋季深翻，将病残体埋入深层土壤；合理密植。\n" +
                       "2. 化学防治：发病初期喷洒50%异菌脲1500倍液或75%肟菌·戊唑醇3000倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：种子处理，平衡施肥，避免过量氮肥。";

            case "corn maize healthy":
                return "玉米植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意防治玉米螟、蚜虫等害虫，合理灌溉，适时追肥。";

            // ============ 葡萄病害 ============
            case "grape black rot":
                return "1. 农业防治：冬季清园，剪除病枝、病叶、病果，集中销毁；合理修剪保持通风。\n" +
                       "2. 化学防治：萌芽前喷5度石硫合剂；发病初期喷洒40%氟硅唑8000倍液或10%苯醚甲环唑1500倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：选用抗病品种，雨季注意排水，避免叶片长时间湿润。";

            case "grape esca black measles":
                return "1. 农业防治：冬季修剪时彻底清除病枝，伤口涂抹愈合剂；避免机械损伤。\n" +
                       "2. 化学防治：春季展叶期喷洒50%多菌灵800倍液或70%甲基硫菌灵800倍液预防；发病初期刮除病斑并涂抹杀菌剂。\n" +
                       "3. 预防措施：选用抗病砧木，避免环剥过度，保持树体健壮。";

            case "grape leaf blight isariopsis leaf spot":
                return "1. 农业防治：清除落叶、病枝，减少越冬菌源；合理修剪，改善通风透光。\n" +
                       "2. 化学防治：发病初期喷洒80%代森锰锌600倍液或70%甲基硫菌灵800倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：雨季前开始预防，注意排水，避免叶片沾水。";

            case "grape healthy":
                return "葡萄植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意霜霉病、白腐病等常见病害预防，定期巡查，及时处理病叶。";

            // ============ 柑橘病害 ============
            case "orange haunglongbing citrus greening":
                return "1. 农业防治：立即挖除病株并销毁（黄龙病无有效治疗）；清除果园内所有病树、弱树。\n" +
                       "2. 化学防治：无 curative 治疗方法；对健康树定期喷洒杀虫剂（如吡虫啉）防治木虱传播。\n" +
                       "3. 预防措施：种植无病苗，严格防控柑橘木虱，建立隔离带，禁止病区调运苗木。";

            // ============ 桃子病害 ============
            case "peach bacterial spot":
                return "1. 农业防治：冬季清园，清除病枝、病叶、病果；避免树冠创伤；合理修剪。\n" +
                       "2. 化学防治：萌芽前喷3-5度石硫合剂；发病初期喷洒72%农用链霉素可溶性粉剂3000倍液或3%中生菌素800倍液，间��7-10天喷一次。\n" +
                       "3. 预防措施：选用抗病品种，避免偏施氮肥，注意排水。";

            case "peach healthy":
                return "桃树植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意桃小食心虫、蚜虫等害虫防治，合理疏果，保持树势。";

            // ============ 辣椒病害 ============
            case "pepper bell bacterial spot":
                return "1. 农业防治：选用无病种子，种子温汤浸种（55℃温水15分钟）；轮作倒茬；高畦栽培。\n" +
                       "2. 化学防治：发病初期喷洒72%农用链霉素3000倍液或3%中生菌素800倍液，或20%叶枯唑600倍液，间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：避免密植，保持通风，雨季注意排水，避免叶片沾水。";

            case "pepper bell healthy":
                return "辣椒植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意蚜虫、烟青虫等害虫防治，合理追肥，促进开花结果。";

            // ============ 马铃薯病害 ============
            case "potato early blight":
                return "1. 农业防治：选用抗病品种，种子处理（温汤浸种或药剂拌种）；合理密植，避免过量氮肥；及时清除病叶。\n" +
                       "2. 化学防治：发病初期喷洒80%代森锰锌600倍液或75%肟菌·戊唑醇3000倍液，或10%苯醚甲环唑1500倍液，间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：与非茄科作物轮作3年以上，收获后彻底清理田间病残体。";

            case "potato late blight":
                return "1. 农业防治：选用抗病品种，种薯处理（草木灰拌种或药剂浸种）；高畦栽培，合理密植，避免田间积水。\n" +
                       "2. 化学防治：发病前预防性喷洒60%氟吗啉·锰锌可湿性粉剂800倍液；发病初期喷洒68%精甲霜灵·代森锰锌水分散粒剂600倍液或72%霜脲·锰锌可湿性粉剂600倍液，间隔5-7天喷一次，连续2-3次。\n" +
                       "3. 预防措施：控制田间湿度，雨季注意排水，发现病株立即拔除并销毁。";

            case "potato healthy":
                return "马铃薯植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意晚疫病预防（尤其是雨季），适时培土，避免块茎暴露。";

            // ============ 覆盆子 ============
            case "raspberry healthy":
                return "覆盆子植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意灰霉病、茎腐病预防，合理修剪结果枝，保持通风透光。";

            // ============ 大豆 ============
            case "soybean healthy":
                return "大豆植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意蚜虫、豆荚螟等害虫防治，适时灌浆期追肥，防止早衰。";

            // ============ 南瓜病害 ============
            case "squash powdery mildew":
                return "1. 农业防治：选用抗病品种，合理密植，加强通风；避免偏施氮肥，增施磷钾肥。\n" +
                       "2. 化学防治：发病初期喷洒40%氟硅唑8000倍液或10%苯醚甲环唑1500倍液，或25%吡唑醚菌酯2000倍液，间隔7-10天喷一次。\n" +
                       "3. 生物防治：可用小檗碱、枯草芽孢杆菌等生物药剂喷雾。\n" +
                       "4. 预防措施：控制田间湿度，避免傍晚浇水，及时清除病叶。";

            // ============ 草莓病害 ============
            case "strawberry leaf scorch":
                return "1. 农业防治：选用抗病品种，移栽无病苗；合理密植，保持通风；避免连作。\n" +
                       "2. 化学防治：发病初期喷洒75%肟菌·戊唑醇3000倍液或40%氟硅唑8000倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：避免叶片沾水，及时摘除老叶、病叶，保持植株清洁。";

            case "strawberry healthy":
                return "草莓植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意灰霉病、白粉病预防，合理疏果，保持地面清洁。";

            // ============ 番茄病害 ============
            case "tomato bacterial spot":
                return "1. 农业防治：选用抗病品种，种子温汤浸种（52℃温水30分钟）；轮作倒茬（与非茄科作物轮作3年以上）；高畦栽培，避免密植。\n" +
                       "2. 化学防治：发病初期喷洒72%农用链霉素可溶性粉剂3000倍液或3%中生菌素800倍液，或20%叶枯唑600倍液，间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：避免雨天或露水未干时操作，防止人为传播；及时清除病叶、病果。";

            case "tomato early blight":
                return "1. 物理防治：发现初期及时摘除病叶、病枝，带出田外深埋或焚烧。\n" +
                       "2. 化学防治：喷洒80%代森锰锌可湿性粉剂600倍液，或70%甲基硫菌灵可湿性粉剂800倍液，或10%苯醚甲环唑1500倍液，7-10天喷1次，连续2-3次。\n" +
                       "3. 预防措施：选用抗病品种，种子温水浸种消毒（52℃温水30分钟），合理密植，避免过量施用氮肥，注意田间通风透光，收获后彻底清园。";

            case "tomato late blight":
                return "1. 物理防治：发现中心病株立即拔除并销毁，带出田外深埋；及时整枝打杈，保持通风。\n" +
                       "2. 化学防治：发病初期喷洒72.2%霜霉威盐酸盐800倍液，或64%霜脲·锰锌可湿性粉剂600倍液，或68%精甲霜灵·代森锰锌水分散粒剂600倍液，7天喷1次，连续2-3次。\n" +
                       "3. 预防措施：控制田间湿度，避免连作，高畦栽培，雨季注意排水，选用抗病品种。";

            case "tomato leaf mold":
                return "1. 农业防治：选用抗病品种，合理密植，加强通风；避免过量氮肥，增施磷钾肥；及时清除病叶。\n" +
                       "2. 化学防治：发病初期喷洒50%腐霉利1500倍液或40%氟硅唑8000倍液，或25%吡唑醚菌酯2000倍液，间隔7-10天喷一次。\n" +
                       "3. 预防措施：控制温室湿度，避免傍晚浇水，注意通风排湿。";

            case "tomato septoria leaf spot":
                return "1. 农业防治：选用抗病品种，种子处理（温汤浸种或药剂拌种）；清除病残体，减少越冬菌源；合理密植，避免连作。\n" +
                       "2. 化学防治：发病初期喷洒75%肟菌·戊唑醇3000倍液或70%甲基硫菌灵800倍液，或80%代森锰锌600倍液，间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：避免叶片沾水，雨季注意排水，收获后彻底清理田间病残体。";

            case "tomato spider mites two spotted spider mite":
                return "1. 物理防治：清水冲洗叶片背面；释放捕食螨（如智利小植绥螨）生物防治。\n" +
                       "2. 化学防治：点片发生时喷洒20%哒螨灵2000倍液或1.8%阿维菌素3000倍液，或43%联苯肼酯2000倍液，重点喷施叶背面。\n" +
                       "3. 预防措施：保持田间通风，避免干旱，定期巡查，发现红蛛及时处理。";

            case "tomato target spot":
                return "1. 农业防治：选用抗病品种，清除病残体，轮作倒茬；合理密植，避免过量氮肥。\n" +
                       "2. 化学防治：发病初期喷洒75%肟菌·戊唑醇3000倍液或40%氟硅唑8000倍液，间隔7-10天喷一次，连续2次。\n" +
                       "3. 预防措施：加强田间管理，增施磷钾肥，提高植株抗病能力。";

            case "tomato tomato yellow leaf curl virus":
                return "1. 农业防治：选用抗病品种，清除病株并销毁（病毒病无治疗办法）；轮作倒茬；防虫控病。\n" +
                       "2. 化学防治：病毒病无特效药剂，重点是预防传播：喷洒10%吡虫啉2000倍液或25%噻虫嗪3000倍液防治烟粉虱（传播媒介），每7-10天喷一次，连续2-3次。\n" +
                       "3. 预防措施：设置防虫网，覆盖银灰膜驱避烟粉虱；避免在病田留种；加强肥水管理，增强植株抗性。";

            case "tomato tomato mosaic virus":
                return "1. 农业防治：选用抗病品种，种子温汤浸种（70℃热水浸种10分钟）；发现病株立即拔除并销毁，带出田外深埋；轮作倒茬3年以上。\n" +
                       "2. 化学防治：病毒病无特效治疗药剂，可尝试喷洒宁南霉素1000倍液或8%盐酸吗啉胍可湿性粉剂800倍液缓解症状，同时防治蚜虫。\n" +
                       "3. 预防措施：避免人为传播（接触病株后洗手再操作），防蚜虫，选用无病种子，加强田间管理。";

            case "tomato healthy":
                return "番茄植株健康状况良好，未发现病害症状。\n" +
                       "维护建议：注意早疫病、晚疫病、病毒病等常见病害预防，定期巡查，保持田间通风透光。";

            // ============ 背景类 ============
            case "background":
                return "图像中未检测到明确的植物叶片。\n" +
                       "建议：请拍摄清晰的叶片正面或背面照片，确保叶片占据画面主要位置，光线充足无遮挡。";

            // ============ 默认处理 ============
            default:
                return "该病害（" + diseaseName + "）的详细防治方案正在完善中。\n" +
                       "通用建议：\n" +
                       "1. 及时清除病叶、病枝，带出田间销毁，加强通风透光。\n" +
                       "2. 发病初期喷洒广谱杀菌剂（如代森锰锌600倍液、甲基硫菌灵800倍液），间隔7-10天喷一次，连续2-3次。\n" +
                       "3. 选用抗病品种，种子消毒处理，合理密植，避免过量氮肥，注意田间卫生。\n" +
                       "4. 咨询当地农业技术推广部门获取更精准的防治方案。";
        }
    }

}
