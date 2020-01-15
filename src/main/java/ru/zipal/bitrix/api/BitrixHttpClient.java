package ru.zipal.bitrix.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zipal.bitrix.api.response.BitrixResponse;
import ru.zipal.bitrix.api.response.BitrixResponseHandler;
import ru.zipal.bitrix.api.response.ErrorBitrixResponse;
import ru.zipal.bitrix.api.response.SuccessBitrixResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class BitrixHttpClient {
    public static final Logger logger = LoggerFactory.getLogger(BitrixHttpClient.class);

    public static final int TIMEOUT = 30000;
    public static final int NO_REQUEST_DELAY = 0;
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private final boolean logResponse;
    // To control a requests-per-second limit
    private final int requestDelay;

    public BitrixHttpClient() {
        this(false, NO_REQUEST_DELAY);
    }

    public BitrixHttpClient(boolean logResponse, int requestDelay) {
        this.logResponse = logResponse;
        this.requestDelay = requestDelay;
    }

    private JSONObject execute(Request request) throws BitrixApiException {
        try {
            if (requestDelay != NO_REQUEST_DELAY) {
                try {
                    Thread.sleep(requestDelay);
                } catch (InterruptedException e) {
                }
            }
            final BitrixResponse response = request.socketTimeout(TIMEOUT).connectTimeout(TIMEOUT).execute().handleResponse(new BitrixResponseHandler(logResponse));
            if (response instanceof SuccessBitrixResponse) {
                return ((SuccessBitrixResponse) response).getResult();
            } else if (response instanceof ErrorBitrixResponse) {
                if (((ErrorBitrixResponse) response).getStatus() == 401 || ((ErrorBitrixResponse) response).getStatus() == 403) {
                    throw new UnauthorizedBitrixApiException(((ErrorBitrixResponse) response).getBody(), ((ErrorBitrixResponse) response).getStatus());
                } else {
                    throw new BitrixApiHttpException(((ErrorBitrixResponse) response).getBody(), ((ErrorBitrixResponse) response).getStatus());
                }
            } else {
                throw new BitrixApiException("Unkown response type: " + response.getClass());
            }
        } catch (IOException e) {
            throw new BitrixApiException(e);
        }
    }

    public JSONObject post(String url, List<NameValuePair> params) throws BitrixApiException {
        logger.info("post {} with {}", url, params.stream().filter(p -> !p.getName().contains("fileData")).collect(Collectors.toList()));
        return execute(Request.Post(url).bodyForm(params, UTF_8));
    }

    public JSONObject post(String url, JSONObject params) throws BitrixApiException {
        logger.info("post {} with {}", url, params.toString());
        return execute(Request.Post(url).bodyString(params.toString(), ContentType.APPLICATION_JSON));
    }

    public JSONObject get(String url) throws BitrixApiException {
        try {
            return execute(Request.Get(url));
        } catch (BitrixApiHttpException e) {
            if (e.getStatus() == 400) {
                throw new UnauthorizedBitrixApiException(e.getMessage(), e.getStatus());
            } else {
                throw e;
            }
        }
    }
}
