package ucmn.network;

import retrofit2.Response;

/**
 * An object instance representing NetworkResponse.
 */

public class NetworkResponse<T> {

    retrofit2.Response<T> response;

    NetworkResponse(retrofit2.Response<T> response) {
        this.response = response;
    }

    NetworkResponse(retrofit2.Response<T> response, Throwable throwable) {
        this.response = response;
    }

    public static <T> NetworkResponse<T> create(Response<T> response) {
        return new NetworkResponse<>(response);
    }

    public static <T> NetworkResponse<T> error(Response<T> response, Throwable throwable) {
        return new NetworkResponse<>(response, throwable);
    }

    public Response<T> response() {
        return response;
    }

    public T body() {
        if (response == null) {
            return null;
        }
        return response.body();
    }

    public boolean isSuccessful() {
        return response != null && response.isSuccessful();
    }
}
