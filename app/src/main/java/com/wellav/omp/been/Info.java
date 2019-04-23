package com.wellav.omp.been;

import android.util.Log;

import com.jakewharton.disklrucache.Util;
import com.wellav.omp.utils.Utilss;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * project name:  HotelTest
 * Created by Huangqi on 2018/4/24 at 11:08.
 * Email:qi.huang@wellav.com
 */

public class Info {
    private static final String TAG = "Info";

    private int code;
    private String description;
    private List<Promotion> promotionList = new ArrayList<>();

    public class Promotion {
        private String mode;
        private String type;
        private String name;
        private String pic;
        private String context;
        private String context_eng;
        private String description;

        public Promotion(JSONObject promotionJson) throws JSONException {
            try {
                this.mode = Utilss.getJsonDataString(promotionJson, "mode");
                this.type = Utilss.getJsonDataString(promotionJson, "type");
                this.name = Utilss.getJsonDataString(promotionJson, "name");
                this.pic = Utilss.getJsonDataString(promotionJson, "pic");
                this.context = Utilss.getJsonDataString(promotionJson, "context");
                this.context_eng = Utilss.getJsonDataString(promotionJson, "context_eng");
                this.description = Utilss.getJsonDataString(promotionJson, "description");
            } catch (Exception e) {

            }
        }

        public String getMode() {
            return this.mode;
        }

        public String getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        public String getPic() {
            return this.pic;
        }

        public String getContext() {
            return this.context;
        }

        public String getContext_eng() {
            return this.context_eng;
        }

        public String getDescription() {
            return this.description;
        }

        @Override
        public String toString() {
            return "\nmode:" + mode + "\ntype:" + type + "\nname:" + name + "\npic:" + pic + "\ncontext:" + context + "\ncontext_eng:" + context_eng + "\ndescription:" + description;
        }
    }

    public Info(String jsonInfo) {
        try {
            JSONObject info = new JSONObject(jsonInfo);
            this.code = Utilss.getJsonDataInt(info, "code");
            this.description = Utilss.getJsonDataString(info, "description");
            JSONObject data = Utilss.getJsonObject(info, "data");
            if (data != null) {
                JSONArray promotionList = Utilss.getJsonArray(data, "promotionlist");
                if (promotionList != null) {
                    for (int i = 0; i < promotionList.length(); i++) {
                        JSONObject promotionJson = promotionList.getJSONObject(i);
                        Promotion promotion = new Promotion(promotionJson);
                        this.promotionList.add(promotion);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Promotion> getPromotionList() {
        return this.promotionList;
    }
}
