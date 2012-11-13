package com.stretchcom.media.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ServerResource;

public class Utility {
	private static RskyboxClient log = new RskyboxClient();

    public static JsonRepresentation apiError(ServerResource theResource, String theApiStatus){
    	if(theApiStatus == null) {
    		log.error("Utility.apiError", "illegal parameter");
    	}
    	
    	log.debug("apiError(): apiStatus = " + theApiStatus);
    	
    	JSONObject json = new JSONObject();
    	try {
    		// HTTP level errors need no further processing.  Application Level error has returned JSON details
    		if(theResource.getStatus().isSuccess()) {
        		theResource.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
    			json.put(ApiJson.SUCCESS, false);
    			JSONArray errorCodesJsonArray = new JSONArray();
    			JSONObject errorObject = new JSONObject();
    			errorObject.put(ApiJson.CODE, theApiStatus);
    			errorObject.put(ApiJson.CATEGORY, ApiJson.BASIC_CATEGORY);
    			errorCodesJsonArray.put(errorObject);
    			json.put(ApiJson.ERROR_CODES, errorCodesJsonArray);
    		}
		} catch (JSONException e) {
			log.exception("Utility::apiError", "",  e);
		}
		return new JsonRepresentation(json);
	}
    
    // JSON object passed in is enhaned with common fields
    // Success JSON field set to true and an empty ErrorCodes array is returned
    public static JsonRepresentation apiSuccess(ServerResource theResource, JSONObject theJson, Status theStatus){
    	try {
            theResource.setStatus(theStatus);
			theJson.put(ApiJson.SUCCESS, true);
			JSONArray errorCodesJsonArray = new JSONArray();
			theJson.put(ApiJson.ERROR_CODES, errorCodesJsonArray);
		} catch (JSONException e) {
			log.exception("Utility::apiError", "",  e);
		}
		return new JsonRepresentation(theJson);
	}
	
	public static String extractAllDigits(String theInputString) {
		// remove all non-digits from the string
		theInputString = theInputString.replaceAll("\\D", "");
		return theInputString;
	}
	
	// returns true if all characters are digits
	public static Boolean isPhoneNumber(String thePotentialNumber) {
		if(thePotentialNumber == null) {return false;}
		int originalSize = thePotentialNumber.length();
		
		// remove all non-digits from the string
		thePotentialNumber = thePotentialNumber.replaceAll("\\D", "");
		int modifiedSize = thePotentialNumber.length();
		return originalSize == modifiedSize;
	}
	
	public static Boolean doesEmailAddressStartWithPhoneNumber(String theEmailAddress) {
		if(theEmailAddress == null) {return false;}
		
		int index = theEmailAddress.indexOf("@");
		if(index >= 1) {
			String potentialPhoneNumber = theEmailAddress.substring(0, index);
			if(isPhoneNumber(potentialPhoneNumber)) {
				return true;
			}
		}
		
		return false;
	}
	
	// returns email domain name (with the leading "@") if found, otherwise returns null
	public static String getEmailDomainNameFromSmsEmailAddress(String theSmsEmailAddress) {
		if(theSmsEmailAddress == null) {return null;}
		
		int index = theSmsEmailAddress.indexOf("@");
		if(index >= 0) {
			String emailDomainName = theSmsEmailAddress.substring(index);
			return emailDomainName;
		}
		return null;
	}

	public static String encrypt(String thePlainText) {
		String encryptedText = null;
		MessageDigest md = null;
		try {
			// use SHA encryption algorithm
			md = MessageDigest.getInstance("SHA");
			
			// convert input plain text into UTF-8 encoded bytes
			md.update(thePlainText.getBytes("UTF-8"));
			
			// extract the encrypted bytes
			byte raw[] = md.digest();
			
			// convert encrypted bytes to base64 encoded string so data can be stored in the database
			encryptedText = Base64.encodeBase64String(raw);
		} catch (Exception e) {
			log.exception("Utility::encrypt", "",  e);
		}
		return encryptedText;
	}
	
	public static String urlEncode(String theInput) {
		String output = "";
		try {
			output = URLEncoder.encode(theInput, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("urlEncode exception = " + e.getMessage());
		}
		return output;
	}
	
    public static void setCookie(HttpServletResponse theHttpResponse, String theCookieName, String theCookieValue, int theCookieAgeInMilliSeconds){
    	String cookieValue = theCookieValue == null ? "" : theCookieValue;
    	Cookie newCookie = new Cookie(theCookieName, cookieValue);
    	newCookie.setPath("/html5");
    	newCookie.setMaxAge(theCookieAgeInMilliSeconds);
    	theHttpResponse.addCookie(newCookie);
    }
    
    public static String getRskyboxAuthHeader(String theToken) {
    	return getRskyboxAuthHeader(theToken, true);
    }
    
    public static String getRskyboxAuthHeader(String theToken, Boolean theWithBasic) {
        // format: Basic rSkyboxLogin:<token_value> where rSkyboxLogin:<token_value> portion is base64 encoded
    	String phrase = "rSkyboxLogin:" + theToken;
    	String phraseBase64 = null;
		try {
			phraseBase64 = Base64.encodeBase64String(phrase.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			log.error("Utility.getRskyboxAuthHeader", "UnsupportedEncodingException::getRskyboxAuthHeader");
			return null;
		}
		
		String authHeader = null;
		if(theWithBasic) {
			authHeader = "Basic " + phraseBase64;
		} else {
			authHeader = phraseBase64;
		}
		
    	return authHeader;
    }
}
