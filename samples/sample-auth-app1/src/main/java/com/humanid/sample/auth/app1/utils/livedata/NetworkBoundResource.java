package com.humanid.sample.auth.app1.utils.livedata;

import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.android.gms.common.internal.Objects;
import com.humanid.sample.auth.app1.utils.livedata.vo.APIResponse;
import com.humanid.sample.auth.app1.utils.livedata.vo.Resource;

public abstract class NetworkBoundResource<ResultType, RequestType> {

    private Executors appExecutors;

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();

    @MainThread
    public NetworkBoundResource() {
        this.appExecutors = Executors.getInstance();
        init();
    }

    private void init() {
        result.setValue(Resource.loading(null));
        LiveData<ResultType> localSource = loadFromLocal();

        result.addSource(localSource, data -> {
            result.removeSource(localSource);

            if (shouldFetch(data)) {
                fetchFromNetwork(localSource);
            } else {
                result.addSource(localSource, newData -> setValue(Resource.success(newData)));
            }
        });
    }

    @MainThread
    private void setValue(@NonNull Resource<ResultType> newValue) {
        if (Objects.equal(result.getValue(), newValue)) return;
        result.setValue(newValue);
    }

    @NonNull
    @MainThread
    protected abstract LiveData<ResultType> loadFromLocal();

    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    @NonNull
    @MainThread
    protected abstract LiveData<APIResponse<RequestType>> createCall();

    private void fetchFromNetwork(@NonNull LiveData<ResultType> localSource) {
        LiveData<APIResponse<RequestType>> networkSource = createCall();

        result.addSource(localSource, newData -> setValue(Resource.loading(newData)));
        result.addSource(networkSource, response -> {
            result.removeSource(networkSource);
            result.removeSource(localSource);

            if (response != null && response.isSuccessful()) {
                appExecutors.diskIO().execute(() -> {
                    RequestType item = processResponse(response);
                    if (item != null) saveCallResult(item);
                    appExecutors.mainThread().execute(() ->
                            result.addSource(loadFromLocal(),
                                    newData -> setValue(Resource.success(newData))));
                });
            } else {
                onFetchFailed();
                result.addSource(localSource, newData -> {
                    String errorMessage = null;
                    if (response != null && !TextUtils.isEmpty(response.getErrorMessage())) {
                        errorMessage = response.getErrorMessage();
                    }
                    setValue(Resource.error(errorMessage, newData));
                });
            }
        });
    }

    @Nullable
    @WorkerThread
    public RequestType processResponse(@NonNull APIResponse<RequestType> response) {
        return response.getData();
    }

    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    protected void onFetchFailed() {}

    @NonNull
    public final LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }
}
