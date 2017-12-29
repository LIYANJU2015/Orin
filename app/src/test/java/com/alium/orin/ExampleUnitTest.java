package com.alium.orin;

import android.util.Log;

import com.alium.orin.model.Song;
import com.alium.orin.util.Util;

import org.junit.Test;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        ArrayList<Song> arrayList = new ArrayList<>();
//
//        for (int i = 0; i < 923; i++) {
//            Song song = new Song();
//            song.id = i;
//            arrayList.add(song);
//        }
//
//        List<Song> newList = checkQueue(arrayList, 400);
//
//        System.out.println("fisrt id : " + newList.get(0).id
//                + " last id : " + newList.get(newList.size()-1).id);
        //String referer = "https://play.google.com/store/apps/details?id=cc.soonet.bitgp&utm_source=facebook&utm_medium=10&utm_campaign=cccsa&utm_term=1&utm_content=33";
        String referer = "https://play.google.com/store/apps/details?id=com.zuiapps.suite.swift&referrer=utm_source%3DtestSource1%26utm_medium%3DtestMedium1%26utm_term%3Dte";
        referer = URLDecoder.decode("utm_source=(not%20set)", "UTF-8");
        System.out.println(referer);
//        String target = "utm_source=";
//        String newreferer = referer.substring(referer.indexOf(target) + target.length(), referer.length());
//        System.out.println("newreferer::" + newreferer);
//        String source = newreferer.substring(0, newreferer.indexOf("&"));
//        System.out.println("source::>>" + source);
//        Util.parseRefererSource(referer);


    }

    int MAX_QUEUE = 120;

    private List<Song> checkQueue(ArrayList<Song> queue, int startPosition) {
        if (queue.size() <= MAX_QUEUE) {
            return queue;
        }

        int max_half_queue = MAX_QUEUE / 2;

        if (startPosition > max_half_queue && (startPosition + max_half_queue) > queue.size()) {
            return queue.subList(startPosition - max_half_queue
                    - (startPosition + max_half_queue - queue.size()), queue.size());
        } else if (startPosition > max_half_queue && (startPosition + max_half_queue) <= queue.size()) {
            return  queue.subList(startPosition - max_half_queue, startPosition + max_half_queue);
        } else if (startPosition <= max_half_queue) {
            return queue.subList(0,MAX_QUEUE);
        } else {
            return queue;
        }
    }
}