package org.jenkinsci.plugin.viewcloner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.w3c.dom.Document;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Base64;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class ViewCloner extends Builder implements SimpleBuildStep {

    private String replacePatternString;
    private String url;
    private String niewViewName;
    private String password;
    private String username;
    
    @DataBoundConstructor
    public ViewCloner(String url, String replacePatternString, String niewViewName, String password, String username) {
        this.replacePatternString = replacePatternString;
        this.url = url;
        this.niewViewName = niewViewName;
        this.username = username;
        this.password = password;
    }

    public String getNiewViewName() {
        return niewViewName;
    }
    
    public String getReplacePatternString() {
        return replacePatternString;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return Utils.encryptPassword(password);
    }
    
    public String getUrl() {
        return url;
    }
    
    @Override
    public void perform(Run<?, ?> run, FilePath path, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        url = Utils.removeEndSlash(url);
        String urlToParentView = Utils.getUrlToTheParentView(url);
        String authString = username + ":" + Utils.decryptPassword(password);
        String authStringEnc = Base64.encode(authString.getBytes("UTF-8"));
        
        ViewHandler viewHandler = new ViewHandler(listener);
        JobHandler jobHandler = new JobHandler(listener);
        Map<String, String> replacePatternOldNew = Utils.processReplacePatern(replacePatternString);
        
        Document viewConfig = viewHandler.getViewConfig(url, authStringEnc);
        List<String> jobNames = viewHandler.getNamesOfAssignedJobs(viewConfig);
        Map<String, Document> jobNameConfig = jobHandler.getJobConfigs(jobNames, authStringEnc);
        
        Map<String, Document> newJobNameConfig = jobHandler.changeNamesAndConfigs(jobNameConfig, replacePatternOldNew);
        viewHandler.changeConfig(viewConfig, replacePatternOldNew);
        
        jobHandler.createJobs(newJobNameConfig);
        viewHandler.createView(urlToParentView, niewViewName, viewConfig, authStringEnc);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "View clone";
        }
        
    }

}
