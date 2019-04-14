package org.oucho.radio2.utils;

import android.content.Context;
import android.content.Intent;

import org.oucho.radio2.radio.RadioService;

public class SendIntent {

    public static void sendIntent(Context context, String list[]) {

        Intent intent = new Intent();
        intent.setAction(list[0]);
        intent.putExtra(list[1], list[2]);
        context.sendBroadcast(intent);

    }

    public static void sendIntent(Context context, String list[], Boolean value) {

        Intent intent = new Intent();
        intent.setAction(list[0]);
        intent.putExtra(list[1], value);
        context.sendBroadcast(intent);

    }

    public static void sendActionIntent(Context context, String list[]) {

        Intent player = new Intent(context, RadioService.class);

        player.putExtra("action", list[0]);
        player.putExtra("url", list[1]);
        player.putExtra("name", list[2]);
        context.startService(player);

    }

    public static void sendAddRadio(Context context, String list[]){

        Intent radio = new Intent();
        radio.setAction(list[0]);
        radio.putExtra("url", list[1]);
        radio.putExtra("name", list[2]);
        radio.putExtra("image", list[3]);
        context.sendBroadcast(radio);

    }
}
