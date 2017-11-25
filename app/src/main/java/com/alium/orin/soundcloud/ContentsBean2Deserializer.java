package com.alium.orin.soundcloud;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by liyanju on 2017/11/25.
 */

public class ContentsBean2Deserializer implements JsonDeserializer<HomeSound.ContentsBean2> {

    @Override
    public HomeSound.ContentsBean2 deserialize(JsonElement json, Type typeOfT,
                                                            JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        HomeSound.ContentsBean2 contentsBean2 = new HomeSound.ContentsBean2();
        contentsBean2.setId(jsonObject.get("id").getAsInt());
        contentsBean2.setName(jsonObject.get("name").getAsString());
        contentsBean2.setStyle(jsonObject.get("style").getAsString());
        contentsBean2.setData_type(jsonObject.get("data_type").getAsInt());

        ArrayList<HomeSound.ContentsBeanX.ContentsBean> list = new ArrayList<>();
        JsonArray contents = jsonObject.getAsJsonArray("contents");
        for (int i = 0 ; i < contents.size(); i++) {
            JsonObject jsonObject1 = contents.get(i).getAsJsonObject();
            HomeSound.ContentsBeanX.ContentsBean contentsBean = context.deserialize(jsonObject1, HomeSound.ContentsBeanX.ContentsBean.class);
            contentsBean.position = i;
            contentsBean.id = contentsBean.getAlbum_images().hashCode();
            list.add(contentsBean);
        }
        contentsBean2.setContents(list);

        return contentsBean2;
    }
}
