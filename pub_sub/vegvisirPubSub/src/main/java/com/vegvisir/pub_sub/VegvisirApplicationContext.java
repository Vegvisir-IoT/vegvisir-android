package com.vegvisir.pub_sub;

import java.util.Set;

/**
 * This class contains core information about the application, such as name, desc, and which
 * channel this application has subscribed so far.
 */
public class VegvisirApplicationContext {

    private String appId;

    private String desc;

    private Set<String> channels;

    /**
     * Public Constructor
     *
     * @param appId    : String representation that reflects the application name
     * @param desc     : String
     * @param channels : Set of Strings pertaining to topics the application
     *                 wants updates from the Vegvisir blockchain.
     */
    public VegvisirApplicationContext(String appId, String desc, Set<String> channels) {
        this.appId = appId;
        this.desc = desc;
        this.channels = channels;
    }

    /**
     * updateChannels
     * TODO: The function name does not reflect what it did inside. This should be 'addChannel' instead of 'updateChannel'. Furthermore, app developers can update topics/channels through getChannels(). Therefore, suggest removing this function. Marked deprecated.
     *
     * @param topic String representation
     * @return True iff string was added to channel set
     */
    @Deprecated
    public boolean updateChannels(String topic) {
        int previous = this.channels.size();
        this.channels.add(topic);
        return previous != this.channels.size();
    }

    /*##############################
     *  Getters & Setters         3
     ############################*/
    public String getAppID() {
        return this.appId;
    }

    public String getDesc() {
        return this.desc;
    }

    public Set<String> getChannels() {
        return this.channels;
    }

    public void setAppID(String newAppID) {
        this.appId = newAppID;
    }

    public void setDesc(String newDesc) {
        this.desc = newDesc;
    }

    public void setChannels(Set<String> newChannels) {
        this.channels = newChannels;
    }

}
