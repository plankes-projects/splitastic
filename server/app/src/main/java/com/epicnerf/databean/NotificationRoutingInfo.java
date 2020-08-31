package com.epicnerf.databean;

import com.epicnerf.enums.ClientView;
import org.json.JSONObject;

public class NotificationRoutingInfo {
    public Integer groupId;
    public ClientView view;

    public NotificationRoutingInfo(Integer groupId, ClientView view) {
        this.groupId = groupId;
        this.view = view;
    }

    public String toJson() {
        JSONObject msg = new JSONObject();
        msg.put("groupId", groupId);
        msg.put("view", view);
        return msg.toString();
    }
}
