package com.main.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.response.MessageResponseDto;
import com.common.base.BaseViewModel;
import com.common.utils.LogUtils;
import com.common.utils.SingleLiveEvent;
import com.main.data.Repository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageViewModel extends BaseViewModel {
    private final Repository repository = new Repository();
    private final MutableLiveData<List<MessageResponseDto>> getMessageComment = new MutableLiveData<>();
    private final MutableLiveData<List<MessageResponseDto>> getMessageXT = new MutableLiveData<>();
    private final MutableLiveData<String> getPostAvr = new MutableLiveData<>();
    private final MutableLiveData<List<MessageResponseDto>> getMessageWarn = new MutableLiveData<>();

    private final MutableLiveData<String> haveNotSeeTz = new MutableLiveData<>();

    private final SingleLiveEvent<String> isRead = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> isReadAllEnable = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> isReadMF = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> isReadAll = new SingleLiveEvent<>();

    private final MutableLiveData<String> haveNotSeeXT = new MutableLiveData<>();
    private final MutableLiveData<List<MessageResponseDto>> getMessage = new MutableLiveData<>();
    private int currentMesUser = 0;
    private boolean isLoaddingMU = false;

    private boolean isHasNextMU = true;

    List<MessageResponseDto> lists = new ArrayList<>();
    List<MessageResponseDto> listMas = new ArrayList<>();
    List<MessageResponseDto> listXT = new ArrayList<>();
    List<MessageResponseDto> listWarn = new ArrayList<>();

    private void getMessageUser() {
        Disposable disposable = repository.getMessageUser(currentMesUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() != null && response.getCode() == ServiceCode.SUCCESS) {
                                if (response.getData() != null) {
                                    if (response.getData().getCommentMessages() != null) {
                                        List<MessageResponseDto> list = response.getData().getCommentMessages().getList();
                                        lists.addAll(list);
                                        List<MessageResponseDto> list1 = response.getData().getSystemMessages().getList();
                                        listXT.addAll(list1);
                                        List<MessageResponseDto> list2 = response.getData().getAlertMessages().getList();
                                        listWarn.addAll(list2);
                                        getMessageComment.setValue(lists);
                                        getMessageXT.setValue(listXT);
                                        getMessageWarn.setValue(listWarn);
                                        isLoaddingMU = false;
                                        currentMesUser++;
                                        isHasNextMU = response.getData().getCommentMessages().getHasNext();
                                    }
                                    if (response.getData().getSystemUnreadCount() != null && response.getData().getAlertUnreadCount() != null&& response.getData().getSystemUnreadCount() +  response.getData().getAlertUnreadCount()> 0) {
                                        haveNotSeeTz.setValue("yes");
                                    }
                                    else{
                                        haveNotSeeTz.setValue("no");
                                    }
                                    if (response.getData().getSystemUnreadCount() != null && response.getData().getSystemUnreadCount() > 0) {
                                        LogUtils.INSTANCE.d("suwei",  response.getData().getSystemUnreadCount() +"");
                                        haveNotSeeXT.setValue("yes");
                                    }
                                    else{
                                        LogUtils.INSTANCE.d("suwei", "ono");
                                        haveNotSeeXT.setValue("no");
                                    }

                                }

                            } else {
                                LogUtils.INSTANCE.d("mes", "weathernotok");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("mes", error);
                        }
                );

        addDisposable(disposable);
    }

    public void  getPostAvr(Long id,int i){
        Disposable disposable = repository.getPost(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() != null && response.getCode() == ServiceCode.SUCCESS) {
                                if(response.getData()!=null && response.getData().getImages()!=null && response.getData().getImages().size()>0){
                                    getPostAvr.setValue(response.getData().getImages().get(0)+","+i);
                                    LogUtils.INSTANCE.d("hehehe",response.getData().getImages().get(0)+","+i );
                                }

                            } else {
                                LogUtils.INSTANCE.d("www", "notok");
                            }
                        },
                        error -> {

                            LogUtils.INSTANCE.e("www", error);
                        }
                );

        addDisposable(disposable);
    }
    public void ceshi (){
        Disposable disposable = repository.ceshi()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() != null && response.getCode() == ServiceCode.SUCCESS) {
                                LogUtils.INSTANCE.d("www", "ok");
                            } else {
                                LogUtils.INSTANCE.d("www", "notok");
                            }
                        },
                        error -> {

                            LogUtils.INSTANCE.e("www", error);
                        }
                );

        addDisposable(disposable);
    }
    public void isRead(Long i,Boolean isMF) {
        Disposable disposable = repository.isRead(i)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() != null && response.getCode() == ServiceCode.SUCCESS) {
                                if(isMF){
                                    isReadMF.setValue("yes");
                                }
                                else{
                                    isRead.setValue("yes");
                                }
                            } else {
                                isRead.setValue("网络连接出错误");
                            }
                        },
                        error -> {
                            isRead.setValue(error.getMessage());
                            LogUtils.INSTANCE.e("mes", error);
                        }
                );

        addDisposable(disposable);
    }
    public void isReadAll() {
        Disposable disposable = repository.isReadAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() != null && response.getCode() == ServiceCode.SUCCESS) {
                                isReadAll.setValue("true");

                            } else {
                                isReadAll.setValue("网络连接出错误~");
                            }
                            isReadAllEnable.setValue("yes");
                        },
                        error -> {
                            isReadAll.setValue(error.getMessage());
                            LogUtils.INSTANCE.e("mes", error);
                            isReadAllEnable.setValue("yes");
                        }
                );

        addDisposable(disposable);
    }

    public void getFirstMessageUser() {
        currentMesUser = 0;
        isHasNextMU = true;
        lists.clear();
        listXT.clear();
        listWarn.clear();
        getMessageUser();
    }

    public void getMoreMessageUser() {
        if (isLoaddingMU) {
            return;
        }
        if (!isHasNextMU) {
            return;
        }
        getMessageUser();
    }


    public MutableLiveData<String> getHaveNotSeeTz() {
        return haveNotSeeTz;
    }

    public MutableLiveData<String> getHaveNotSeeXT() {
        return haveNotSeeXT;
    }

    public MutableLiveData<List<MessageResponseDto>> getGetMessageComment() {
        return getMessageComment;
    }

    public MutableLiveData<List<MessageResponseDto>> getGetMessage() {
        return getMessage;
    }

    public MutableLiveData<List<MessageResponseDto>> getGetMessageWarn() {
        return getMessageWarn;
    }

    public MutableLiveData<List<MessageResponseDto>> getGetMessageXT() {
        return getMessageXT;
    }

    public SingleLiveEvent<String> getIsRead() {
        return isRead;
    }

    public SingleLiveEvent<String> getIsReadAll() {
        return isReadAll;
    }

    public MutableLiveData<String> getGetPostAvr() {
        return getPostAvr;
    }

    public SingleLiveEvent<String> getIsReadMF() {
        return isReadMF;
    }

    public SingleLiveEvent<String> getIsReadAllEnable() {
        return isReadAllEnable;
    }
}
