package com.android.support;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    //Only if you have changed MainActivity to yours and you wanna call game's activity.
    // LEAVE EMPTY for Universal Mod App (Overlay Only)
    public String GameActivity = "";
    public boolean hasLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //To launch game activity
        if (!hasLaunched) {
            hasLaunched = true;
            if (!GameActivity.isEmpty()) {
                try {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, Class.forName(MainActivity.this.GameActivity)));
                } catch (ClassNotFoundException e) {
                    Log.e("Mod_menu", "Error. Game's main activity does not exist");
                }
            }
        }

        //Launch mod menu.
        Main.Start(this);
    }
}
