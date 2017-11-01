package edu.neu.ccs.wellness.server;

/**
 * Created by hermansaksono on 11/1/17.
 */

public class OAuth2Exception extends Exception {
    public OAuth2Exception() {}
    public OAuth2Exception(String msg) { super(msg); }
    public OAuth2Exception(Throwable cause) { super(cause); }
    public OAuth2Exception(String msg, Throwable cause) { super(msg, cause); }
}
