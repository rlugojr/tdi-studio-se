// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.salesforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import com.salesforce.soap.partner.CallOptions;
import com.salesforce.soap.partner.GetUserInfoResponse;
import com.salesforce.soap.partner.GetUserInfoResult;
import com.salesforce.soap.partner.Login;
import com.salesforce.soap.partner.LoginResult;
import com.salesforce.soap.partner.SessionHeader;
import com.salesforce.soap.partner.SforceServiceStub;

/**
 * created by bchen on Jul 9, 2014 Detailled comment
 *
 */
public class SforceBasicConnection extends SforceConnection {
    private final String login_endpoint;
    private final Login userInfo;
    private boolean needCompression;
    private boolean useHttpChunked;
    private int timeout;
    private String clientID;
    private String sessionId;
    private String serviceEndPoint;
    private String sessionFilePath;
    public String getServiceEndPoint(){
    	return this.serviceEndPoint;
    }

    private SforceBasicConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SforceBasicConnection(Builder builder) throws Exception {
        this.login_endpoint = builder.login_endpoint;
        this.userInfo = builder.userInfo;
        this.needCompression = builder.needCompression;
        this.useHttpChunked = builder.useHttpChunked;
        this.timeout = builder.timeout;
        this.clientID = builder.clientID;
        if(builder.sessionFilePath != null && builder.sessionFilePath.length() > 0){
        	 this.sessionFilePath = builder.sessionFilePath+"/sessionIDFile_"+this.userInfo.getUsername();
        }
        if(sessionFilePath != null){
        	Properties properties = getSessionProperties();
            if(properties != null){
            	 this.sessionId = properties.getProperty("SESSION_ID");
                 this.serviceEndPoint = properties.getProperty("SERVICE_ENDPOINT");
            }
        }
        check();
        init();
    }

    private void check() throws Exception {
        if (login_endpoint == null || login_endpoint.trim().length() == 0 || userInfo.getUsername() == null
                || userInfo.getUsername().trim().length() == 0 || userInfo.getPassword() == null
                || userInfo.getPassword().trim().length() == 0) {
            throw new RuntimeException("Login failed! Please check the username,password and endpoint");
        }
    }

    private void init() throws Exception {
        if (clientID != null) {
            co = new CallOptions();
            co.setClient(clientID);
        }
        stub = new SforceServiceStub();
        SforceManagementUtil.needCompression(stub, needCompression);
        SforceManagementUtil.setTimeout(stub, timeout);
        SforceManagementUtil.useHttpChunked(stub, useHttpChunked);
        SforceManagementUtil.setHttpProxy(stub);
        sh = new SessionHeader();
        if(sessionId != null || serviceEndPoint != null){
        	sh.setSessionId(sessionId);
        	SforceManagementUtil.setEndpoint(stub, serviceEndPoint);
        }else{
        	renewSession();
        }
    }

    @Override
    protected void renewSession() throws Exception {
        SforceManagementUtil.setEndpoint(stub, login_endpoint);// login_endpoint for login operation
        LoginResult loginResult = stub.login(userInfo, null, co).getResult();
        String sessionId = loginResult.getSessionId();
        sh.setSessionId(sessionId);
        String serviceEndPoint = loginResult.getServerUrl();
        SforceManagementUtil.setEndpoint(stub, serviceEndPoint);// server url for CRUD operation
        
        this.serviceEndPoint = serviceEndPoint;
        this.sessionId = sessionId;
        if(sessionFilePath != null){
        	setupSessionProperties();
        }
        
    }
    
    protected Properties getSessionProperties() throws Exception{
    	File sessionFile = new java.io.File(sessionFilePath);
    	if(sessionFile.exists()){
    		FileInputStream sessionInput = new FileInputStream(sessionFile);
    		Properties sessionProp = new Properties();
    		sessionProp.load(sessionInput);
    		int maxValidSeconds = Integer.valueOf(sessionProp.getProperty("MAX_VALID_SECONDS"));
    		if(maxValidSeconds > ((System.currentTimeMillis() - sessionFile.lastModified())/1000)){
    			return sessionProp;
    		}
    		
    	}
    	return null;
    }
    
    protected void setupSessionProperties() throws Exception{
    	GetUserInfoResponse reponse = getUserInfo();
        GetUserInfoResult result = reponse.getResult();
        File sessionFile = new java.io.File(sessionFilePath);
        if(!sessionFile.exists()){
        	File parentPath = sessionFile.getParentFile();
        	if(!parentPath.exists()){
        		parentPath.mkdirs();
        	}
        	sessionFile.createNewFile();
        }
    	FileOutputStream sessionOutput = new FileOutputStream(sessionFile);
    	Properties sessionProp = new Properties();
    	sessionProp.setProperty("SESSION_ID",sessionId);
    	sessionProp.setProperty("SERVICE_ENDPOINT",serviceEndPoint);
    	sessionProp.setProperty("MAX_VALID_SECONDS",String.valueOf(result.getSessionSecondsValid()));
    	try{
    		sessionProp.store(sessionOutput, null);
    	}finally{
    		sessionOutput.close();
    	}
    }

    public static class Builder {
        private final String login_endpoint;
        private final Login userInfo;
        private boolean needCompression = false;
        private boolean useHttpChunked;
        private int timeout = 60000;
        private String clientID = null;
        private String sessionFilePath;

        public Builder(String login_endpoint, String username, String password) {
            this.login_endpoint = login_endpoint;
            this.userInfo = new Login();
            this.userInfo.setUsername(username);
            this.userInfo.setPassword(password);
        }

        public Builder needCompression(boolean needCompression) {
            this.needCompression = needCompression;
            return this;
        }

        public Builder useHttpChunked(boolean useHttpChunked){
        	this.useHttpChunked = useHttpChunked;
        	return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setTimeout(String timeout) {
            this.timeout = Integer.valueOf(timeout);
            return this;
        }

        public Builder setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

		public Builder setSessionFilePath(String sessionFilePath) {
			this.sessionFilePath = sessionFilePath;
			return this;
		}

		public SforceBasicConnection build() throws Exception {
            return new SforceBasicConnection(this);
        }

    }
}
