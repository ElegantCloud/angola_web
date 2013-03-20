package com.richitec.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	private static Log log = LogFactory.getLog(HttpUtils.class);

	// singleton instance
	private static HttpUtils _singletonInstance;

	// apache default http client
	private HttpClient _mDefaultHttpClient;

	// connection and socket timeout
	private int _mTimeoutConnection = 5000;
	private int _mTimeoutSocket = 10000;

	private HttpUtils() {
		// init http param
		HttpParams _httpParameters = new BasicHttpParams();
		// set timeout
		HttpConnectionParams.setConnectionTimeout(_httpParameters,
				_mTimeoutConnection);
		HttpConnectionParams.setSoTimeout(_httpParameters, _mTimeoutSocket);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(
				_httpParameters, registry);

		// PoolingClientConnectionManager connManager = new
		// PoolingClientConnectionManager(
		// registry);
		// connManager.setMaxTotal(100);
		// connManager.setDefaultMaxPerRoute(100);
		// init http client
		_mDefaultHttpClient = new DefaultHttpClient(cm, _httpParameters);
	}

	private HttpClient getDefaultHttpClient() {
		return _mDefaultHttpClient;
	}

	// get apache http client
	private static HttpClient getHttpClient() {
		if (null == _singletonInstance) {
			synchronized (HttpUtils.class) {
				if (null == _singletonInstance) {
					_singletonInstance = new HttpUtils();
				}
			}
		}

		return _singletonInstance.getDefaultHttpClient();
	}

	// send get request
	public static HttpResponseResult getRequest(String pUrl,
			Map<String, String> pParam) {
		// perfect http request url
		StringBuilder _httpRequestUrl = new StringBuilder();
		_httpRequestUrl.append(pUrl);
		// append char '?' first and param pairs, if param not null
		if (null != pParam && !pParam.isEmpty()) {
			_httpRequestUrl.append('?');

			// append param pairs
			for (String _paramKey : pParam.keySet()) {
				_httpRequestUrl.append(_paramKey + "=" + pParam.get(_paramKey)
						+ '&');
			}

			// trim last char '&' in http request url
			_httpRequestUrl.deleteCharAt(_httpRequestUrl.length() - 1);
		}

		// new httpGet object
		HttpGet _getHttpRequest = new HttpGet(_httpRequestUrl.toString());

		// send synchronous get request
		HttpResponseResult responseResult = new HttpResponseResult();
		try {
			HttpResponse _response = getHttpClient().execute(_getHttpRequest);
			responseResult.setStatusCode(_response.getStatusLine()
					.getStatusCode());
			responseResult
					.setResponseText(getHttpResponseEntityString(_response));
			if (_response != null) {
				_response.getEntity().consumeContent();
			}

		} catch (Exception e) {
			log.debug("Send synchronous get request excetion message = "
					+ e.getMessage());

			e.printStackTrace();

			_getHttpRequest.abort();
		}
		return responseResult;

	}

	// send post request
	public static HttpResponseResult postRequest(String pUrl,
			PostRequestFormat pPostFormat, Map<String, String> pParam) {
		// new httpPost object
		HttpPost _postHttpRequest = new HttpPost(pUrl);

		// check param and set post request param
		if (null != pParam && !pParam.isEmpty()) {
			try {
				switch (pPostFormat) {
				case URLENCODED: {
					// define urlEncodedForm post request param
					List<NameValuePair> _urlEncodedFormPostReqParam = new ArrayList<NameValuePair>();

					// update urlEncodedForm post request param
					for (String _paramKey : pParam.keySet()) {
						_urlEncodedFormPostReqParam.add(new BasicNameValuePair(
								_paramKey, pParam.get(_paramKey)));
					}

					// set entity
					_postHttpRequest.setEntity(new UrlEncodedFormEntity(
							_urlEncodedFormPostReqParam, HTTP.UTF_8));
				}

					break;

				case MULTIPARTFORMDATA: {
					// init multipart entity
					MultipartEntity _multipartEntity = new MultipartEntity();

					// update multipart entity
					for (String _paramKey : pParam.keySet()) {
						_multipartEntity.addPart(
								_paramKey,
								new StringBody(pParam.get(_paramKey), Charset
										.forName(HTTP.UTF_8)));
					}

					// set entity
					_postHttpRequest.setEntity(_multipartEntity);
				}
					break;
				}
			} catch (UnsupportedEncodingException e) {
				log.debug("Post request post body unsupported encoding exceptio message = "
						+ e.getMessage());

				e.printStackTrace();
			}

		}

		// send synchronous post request
		HttpResponseResult responseResult = new HttpResponseResult();
		try {
			HttpResponse _response = getHttpClient().execute(_postHttpRequest);
			responseResult.setStatusCode(_response.getStatusLine()
					.getStatusCode());
			responseResult
					.setResponseText(getHttpResponseEntityString(_response));
			if (_response != null) {
				_response.getEntity().consumeContent();
			}
		} catch (Exception e) {
			log.debug("Send synchronous post request excetion message = "
					+ e.getMessage());

			e.printStackTrace();
			_postHttpRequest.abort();
		}
		return responseResult;
	}

	// get http response entity string
	public static String getHttpResponseEntityString(HttpResponse response) {
		String _respEntityString = "";

		// check response
		if (null != response) {
			try {
				_respEntityString = EntityUtils.toString(response.getEntity(),
						HTTP.UTF_8);
			} catch (Exception e) {
				log.debug("Get http response entity excetion message = "
						+ e.getMessage());

				e.printStackTrace();
			}
		} else {
			log.debug("Get http response entity, response is null");
		}

		return _respEntityString;
	}

	// post request format
	public enum PostRequestFormat {
		URLENCODED, MULTIPARTFORMDATA
	}

	/**
	 * http response result set
	 * 
	 * @author star
	 * 
	 */
	public static class HttpResponseResult {
		private int statusCode;
		private String responseText;

		public HttpResponseResult() {
			statusCode = -1;
			responseText = "";
		}

		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public String getResponseText() {
			return responseText;
		}

		public void setResponseText(String responseText) {
			this.responseText = responseText;
		}

	}

}
