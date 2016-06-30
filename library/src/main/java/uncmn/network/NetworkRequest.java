package uncmn.network;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * An object instance representing NetworkRequest.
 */
public abstract class NetworkRequest<T> {

  public interface Callback<T> {

    void onSuccess(NetworkResponse<T> t);

    void onFailure(Throwable t);
  }

  private final int EXECUTE = 0x01;
  private final int OBSERVABLE = 0x02;
  private int requestType = 0;
  private Call call;
  private Observable<NetworkResponse<T>> observable;

  /**
   * Execute retrofit call. Makes a network request
   *
   * @param callback {@link Callback}
   */
  public final void execute(Callback<T> callback) {
    if (!validate()) {
      callback.onFailure(new RuntimeException("Validation failed"));
      return;
    }
    requestType = EXECUTE;
    if (callback == null) {
      return;
    }
    this.call = retrofitCall();
    this.call.enqueue(retrofitCallback(callback));
  }

  /**
   * Makes a network request and returns an observable
   *
   * @return {@link Observable}
   */
  public final Observable<NetworkResponse<T>> asObservable() {
    if (!validate()) {
      return Observable.error(new RuntimeException("Validation failed"));
    }
    requestType = OBSERVABLE;
    this.observable = observable();
    return observable == null ? Observable.<NetworkResponse<T>>empty() : observable;
  }

  /**
   * Cancel a retrofit request
   */
  public final void cancel() {
    switch (requestType) {
      case EXECUTE: {
        if (call != null) {
          call.cancel();
        }
        break;
      }
      case OBSERVABLE: {
        if (observable != null) {
          observable.unsafeSubscribe(new Subscriber<NetworkResponse<T>>() {
            @Override public void onCompleted() {

            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(NetworkResponse<T> tNetworkResponse) {

            }
          });
          break;
        }
      }
    }
  }

  /**
   * Cancel a Subscription
   *
   * @param subscription {@link Subscription}
   */

  public final void cancel(Subscription subscription) {
    if (subscription != null) {
      subscription.unsubscribe();
    }
  }

  /**
   * Validate fields before making a request
   *
   * @return true if all the fields are validated false otherwise
   */
  protected abstract boolean validate();

  /**
   * Get Observable from retrofit service
   *
   * @return {@link Observable}
   */
  protected abstract Observable<NetworkResponse<T>> observable();

  /**
   * Get Retrofit {@link Call} for the current request
   *
   * @return {@link Call}
   */
  protected abstract Call retrofitCall();

  protected abstract retrofit2.Callback retrofitCallback(Callback<T> callback);

  /**
   * Return Empty response
   */
  protected abstract NetworkResponse<T> emptyResponse();

  /**
   * Return the generic retrofit callback for the T type
   */
  protected final retrofit2.Callback<T> genericRetrofitCallback(final Callback<T> callback) {
    return new retrofit2.Callback<T>() {
      @Override public void onResponse(Call<T> call, Response<T> response) {
        if (response == null) {
          ResponseBody rawBody = ResponseBody.create(MediaType.parse("application/json"), "{}");
          Response<T> r = Response.error(400, rawBody);
          callback.onSuccess(NetworkResponse.create(r));
        } else {
          callback.onSuccess(NetworkResponse.create(response));
        }
      }

      @Override public void onFailure(Call<T> call, Throwable t) {
        callback.onFailure(t);
        call.cancel();
      }
    };
  }
}

