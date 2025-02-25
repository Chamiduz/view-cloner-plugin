package org.jenkinsci.plugin.viewcloner;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class JobHandler {
    private Jenkins jenkins;
    private PrintStream logger;
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y' };
    
    JobHandler(TaskListener listener) {
        this.jenkins = Jenkins.getInstance();
        this.logger = listener.getLogger();
    }
    
    public Map<String, Document> getJobConfigs(List<String> jobNames, String authStringEnc) {
        String url;
        Map<String, Document> map = new HashMap<String, Document>();
        logger.println("[Get job configs]");
        for(String jobName : jobNames){
            if(jenkins.getItem(jobName) != null){
                url = jenkins.getRootUrl() + "job/" + jobName;
                Document xml = Utils.getConfig(url, authStringEnc);
                logger.println("Successfully acquired job config from " + url + Utils.CONFIG_XML_PATH);
                map.put(jobName, xml);
            }    
        }
        return map;
    }

    public Map<String, Document> changeNamesAndConfigs(Map<String, Document> jobNameConfig, Map<String, String> replacePatternOldNew) {
        Map<String, Document> newJobNameConfig = new HashMap<String, Document>();
        Iterator<?> it = jobNameConfig.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Document> pair = (Entry<String, Document>) it.next();
            Utils.changeConfig(pair.getValue(), replacePatternOldNew); // change config.xml
            String newName = Utils.paramChange(pair.getKey(), replacePatternOldNew);  // change job name
            newJobNameConfig.put(newName, pair.getValue());
        }
        return newJobNameConfig;
    }

    public void createJobs(Map<String, Document> jobNameConfig) {
        Iterator<?> it = jobNameConfig.entrySet().iterator();
        logger.println("[Create jobs]");
        while (it.hasNext()) {
            Map.Entry<String, Document> pair = (Entry<String, Document>) it.next();
            if(jenkins.getItem(pair.getKey()) == null){
                try {
                    jenkins.createProjectFromXML(pair.getKey(), Utils.docToStream(pair.getValue()));
                    logger.println("Job "+ pair.getKey() + " created");
                } catch (IOException | TransformerFactoryConfigurationError e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.println("Job with name " + pair.getKey() + " already exists");
            }
        }
    }

    private Document encryptPasswordsInConfig(Document jobConfig) {
        // Implement encryption logic here, e.g., using AES
        // This is a placeholder for the actual encryption implementation
        return jobConfig;
    }

    private Document decryptPasswordsInConfig(Document jobConfig) {
        // Implement decryption logic here, e.g., using AES
        // This is a placeholder for the actual decryption implementation
        return jobConfig;
    }

    private static String encrypt(String valueToEnc) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = new SecretKeySpec(keyValue, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encValue = cipher.doFinal(valueToEnc.getBytes());
        return Base64.getEncoder().encodeToString(encValue);
    }

    private static String decrypt(String encryptedValue) throws Exception {
        SecretKey secretKey = new SecretKeySpec(keyValue, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decValue = Base64.getDecoder().decode(encryptedValue);
        byte[] decValueBytes = cipher.doFinal(decValue);
        return new String(decValueBytes);
    }
}