//Please don't replace listeners with lambda!

package com.android.support;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

import org.xml.sax.ErrorHandler;

public class Menu {
    //********** KONFIGURASI TAMPILAN: ULTIMATE iOS GLASS STYLE (MERGED) **********//

    //region Variable
    public static final String TAG = "Mod_Menu"; //Tag for logcat

    // --- PALET WARNA PREMIUM (Dark Glass) ---
    int MENU_BG_COLOR = Color.parseColor("#E6121212"); 
    int BORDER_COLOR = Color.parseColor("#1AFFFFFF"); 
    int TEXT_COLOR = Color.parseColor("#F2F2F7"); 
    int TEXT_COLOR_2 = Color.parseColor("#8E8E93"); 
    int ACCENT_COLOR = Color.parseColor("#0A84FF"); 
    int SWITCH_ON_COLOR = Color.parseColor("#30D158"); 
    int BTN_COLOR = Color.parseColor("#1C1C1E"); 
    int SEPARATOR_COLOR = Color.parseColor("#2C2C2E");

    // Compatibility fields from Code 1
    int MENU_FEATURE_BG_COLOR = Color.TRANSPARENT;
    int ToggleON = SWITCH_ON_COLOR;
    int ToggleOFF = Color.parseColor("#3A3A3C");
    int BtnON = ACCENT_COLOR;
    int BtnOFF = BTN_COLOR;
    int CategoryBG = Color.TRANSPARENT;
    int SeekBarColor = Color.WHITE;
    int SeekBarProgressColor = ACCENT_COLOR;
    int CheckBoxColor = ACCENT_COLOR;
    int RadioColor = ACCENT_COLOR;
    int CollapseColor = Color.parseColor("#1AFFFFFF");
    String NumberTxtColor = "#0A84FF"; // ACCENT_COLOR as string

    // --- DIMENSI & UX ---
    int MENU_WIDTH = 340; 
    int MENU_HEIGHT = 280; 
    float MENU_CORNER = 40f; 
    int ICON_SIZE = 55; 
    float ICON_ALPHA = 0.9f; 

    // Reflection compatibility fields
    int POS_X = 0;
    int POS_Y = 100;
    //********************************************************************//


    RelativeLayout mCollapsed, mRootContainer;
    LinearLayout mExpanded, mods, mSettings, mCollapse;
    LinearLayout.LayoutParams scrlLLExpanded, scrlLL;
    WindowManager mWindowManager;
    WindowManager.LayoutParams vmParams;
    ImageView startimage;
    FrameLayout rootFrame;
    ScrollView scrollView;
    boolean stopChecking, overlayRequired;
    Context getContext;
    Vibrator vibrator;

    //initialize methods from the native library
    native void Init(Context context, TextView title, TextView subTitle);

    native String Icon();

    native String IconWebViewData();

    native String[] GetFeatureList();

    native String[] SettingsList();

    native boolean IsGameLibLoaded();

    //Here we write the code for our Menu
    // Reference: https://www.androidhive.info/2016/11/android-floating-widget-like-facebook-chat-head/
    public Menu(Context context) {

        getContext = context;
        Preferences.context = context;

        // Inisialisasi Vibrator dengan aman (Anti Crash)
        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            Log.e(TAG, "Vibrator service not available");
        }

        rootFrame = new FrameLayout(context); // Global markup
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(context); // Markup on which two markups of the and the menu itself will be placed
        mCollapsed = new RelativeLayout(context); // Markup of the icon (when the menu is minimized)
        mCollapsed.setVisibility(View.VISIBLE);
        mCollapsed.setAlpha(ICON_ALPHA);

        //********** The box of the mod menu **********
        mExpanded = new LinearLayout(context); // Menu markup (when the menu is expanded)
        mExpanded.setVisibility(View.GONE);
        mExpanded.setOrientation(LinearLayout.VERTICAL);
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(dp(MENU_WIDTH), WRAP_CONTENT));

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        mExpanded.setLayoutTransition(transition);

        GradientDrawable gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(MENU_CORNER); //Set corner
        gdMenuBody.setColor(MENU_BG_COLOR); //Set background color
        gdMenuBody.setStroke(2, BORDER_COLOR); //Set border
        mExpanded.setBackground(gdMenuBody); //Apply GradientDrawable to it

        //********** The icon to open mod menu **********
        startimage = new ImageView(context);
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension = (int) TypedValue.applyDimension(1, ICON_SIZE, context.getResources().getDisplayMetrics()); //Icon size
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        //startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    vibrate(30);
                    mCollapsed.setVisibility(View.GONE);
                    mExpanded.setVisibility(View.VISIBLE);
                }
            });

        //********** The icon in Webview to open mod menu **********
        WebView wView = new WebView(context); //Icon size width=\"50\" height=\"50\"
        wView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension2 = (int) TypedValue.applyDimension(1, ICON_SIZE, context.getResources().getDisplayMetrics()); //Icon size
        wView.getLayoutParams().height = applyDimension2;
        wView.getLayoutParams().width = applyDimension2;
        wView.loadData("<html>" +
                       "<head></head>" +
                       "<body style=\"margin: 0; padding: 0\">" +
                       "<img src=\"" + IconWebViewData() + "\" width=\"" + ICON_SIZE + "\" height=\"" + ICON_SIZE + "\" >" +
                       "</body>" +
                       "</html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setAlpha(ICON_ALPHA);
        wView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wView.setOnTouchListener(onTouchListener());

        // --- HEADER ---
        RelativeLayout headerLayout = new RelativeLayout(context);
        headerLayout.setPadding(40, 40, 40, 10);

        TextView title = new TextView(context);
        title.setTextColor(TEXT_COLOR);
        title.setTextSize(20.0f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setText("Mod Menu");
        RelativeLayout.LayoutParams rlTitle = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlTitle.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        title.setLayoutParams(rlTitle);

        TextView settingsBtn = new TextView(context);
        settingsBtn.setText(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? "≡" : "⚙");
        settingsBtn.setTextColor(TEXT_COLOR_2);
        settingsBtn.setTextSize(22.0f);
        settingsBtn.setPadding(20, 0, 0, 20);
        RelativeLayout.LayoutParams rlSettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlSettings.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        settingsBtn.setLayoutParams(rlSettings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
                boolean settingsOpen;

                @Override
                public void onClick(View v) {
                    try {
                        vibrate(20);
                        settingsOpen = !settingsOpen;
                        if (settingsOpen) {
                            scrollView.removeView(mods);
                            scrollView.addView(mSettings);
                            scrollView.scrollTo(0, 0);
                        } else {
                            scrollView.removeView(mSettings);
                            scrollView.addView(mods);
                        }
                    } catch (IllegalStateException e) {
                    }
                }
            });

        headerLayout.addView(title);
        headerLayout.addView(settingsBtn);

        //********** Sub title **********
        TextView subTitle = new TextView(context);
        subTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        subTitle.setMarqueeRepeatLimit(-1);
        subTitle.setSingleLine(true);
        subTitle.setSelected(true);
        subTitle.setTextColor(TEXT_COLOR_2);
        subTitle.setTextSize(11.0f);
        subTitle.setPadding(40, 0, 40, 20);
        subTitle.setTypeface(Typeface.MONOSPACE);

        //********** Mod menu feature list **********
        scrollView = new ScrollView(context);
        //Auto size. To set size manually, change the width and height example 500, 500
        scrlLL = new LinearLayout.LayoutParams(MATCH_PARENT, dp(MENU_HEIGHT));
        scrlLLExpanded = new LinearLayout.LayoutParams(mExpanded.getLayoutParams());
        scrlLLExpanded.weight = 1.0f;
        scrollView.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView.setBackgroundColor(Color.TRANSPARENT);
        scrollView.setVerticalScrollBarEnabled(false); 
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mods = new LinearLayout(context);
        mods.setOrientation(LinearLayout.VERTICAL);
        mods.setPadding(0, 10, 0, 10);

        //********** Settings **********
        mSettings = new LinearLayout(context);
        mSettings.setOrientation(LinearLayout.VERTICAL);
        featureList(SettingsList(), mSettings);

        // --- FOOTER ---
        LinearLayout footerLayout = new LinearLayout(context);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);
        footerLayout.setGravity(Gravity.CENTER);
        footerLayout.setPadding(0, 20, 0, 30);

        Button hideBtn = new Button(context);
        hideBtn.setText("HIDE");
        hideBtn.setTextColor(ACCENT_COLOR);
        hideBtn.setBackgroundColor(Color.TRANSPARENT);
        hideBtn.setTextSize(12f);
        hideBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    vibrate(30);
                    mCollapsed.setVisibility(View.VISIBLE);
                    mCollapsed.setAlpha(0);
                    mExpanded.setVisibility(View.GONE);
                    showCustomToast("Hidden Mod. Remember its spot.");
                }
            });
        hideBtn.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    vibrate(100);
                    showCustomToast("Menu killed");
                    rootFrame.removeView(mRootContainer);
                    mWindowManager.removeView(rootFrame);
                    return true;
                }
            });

        Button closeBtn = new Button(context);
        closeBtn.setText("MINIMIZE");
        closeBtn.setTextColor(Color.parseColor("#FF453A")); 
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        closeBtn.setTextSize(12f);
        closeBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    vibrate(20);
                    mCollapsed.setVisibility(View.VISIBLE);
                    mCollapsed.setAlpha(ICON_ALPHA);
                    mExpanded.setVisibility(View.GONE);
                }
            });

        //********** Adding view components **********
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }

        mExpanded.addView(headerLayout);
        mExpanded.addView(subTitle);
        scrollView.addView(mods);
        mExpanded.addView(scrollView);

        footerLayout.addView(hideBtn);
        footerLayout.addView(closeBtn);
        mExpanded.addView(footerLayout);

        Init(context, title, subTitle);
    }

    public void ShowMenu() {
        rootFrame.addView(mRootContainer);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                boolean viewLoaded = false;

                @Override
                public void run() {
                    //If the save preferences is enabled, it will check if game lib is loaded before starting menu
                    //Comment the if-else code out except startService if you want to run the app and test preferences
                    if (Preferences.loadPref && !IsGameLibLoaded() && !stopChecking) {
                        if (!viewLoaded) {
                            Category(mods, "Save preferences was been enabled. Waiting for game lib to be loaded...\n\nForce load menu may not apply mods instantly. You would need to reactivate them again");
                            Button(mods, -100, "Force load menu");
                            viewLoaded = true;
                        }
                        handler.postDelayed(this, 600);
                    } else {
                        mods.removeAllViews();
                        featureList(GetFeatureList(), mods);
                    }
                }
            }, 500);
    }

    @SuppressLint("WrongConstant")
    public void SetWindowManagerWindowService() {
        //Variable to check later if the phone supports Draw over other apps permission
        int iparams = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ? 2038 : 2002;
        vmParams = new WindowManager.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
            iparams,
            8 | 0x00000040, // Added 0x00000040 from Code 2
            -3);
        //params = new WindowManager.LayoutParams(WindowManager.LayoutParams.LAST_APPLICATION_WINDOW, 8, -3);
        vmParams.gravity = 51;
        vmParams.x = POS_X;
        vmParams.y = POS_Y;

        mWindowManager = (WindowManager) getContext.getSystemService(getContext.WINDOW_SERVICE);
        mWindowManager.addView(rootFrame, vmParams);

        overlayRequired = true;
    }

    @SuppressLint("WrongConstant")
    public void SetWindowManagerActivity() {
        vmParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            POS_X,//initialX
            POS_Y,//initialy
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
            PixelFormat.TRANSPARENT
        );
        vmParams.gravity = 51;
        vmParams.x = POS_X;
        vmParams.y = POS_Y;

        mWindowManager = ((Activity) getContext).getWindowManager();
        mWindowManager.addView(rootFrame, vmParams);
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = vmParams.x;
                        initialY = vmParams.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        mExpanded.setAlpha(1f);
                        mCollapsed.setAlpha(ICON_ALPHA);
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            //When user clicks on the image view of the collapsed layout,
                            //visibility of the collapsed layout will be changed to "View.GONE"
                            //and expanded view will become visible.
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mExpanded.setAlpha(0.9f);
                        mCollapsed.setAlpha(0.5f);
                        //Calculate the X and Y coordinates of the view.
                        vmParams.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        vmParams.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, vmParams);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    private void featureList(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            //Log.i("featureList", listFT[i]);
            String feature = listFT[i];
            if (feature.contains("_True")) {
                switchedOn = true;
                feature = feature.replaceFirst("_True", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd_")) {
                //if (collapse != null)
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd_", "");
            }
            String[] str = feature.split("_");

            //Assign feature number
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                //Subtract feature number. We don't want to count ButtonLink, Category, RichTextView and RichWebView
                featNum = i - subFeat;
            }
            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle":
                    Switch(linearLayout, featNum, strSplit[1], switchedOn);
                    break;
                case "SeekBar":
                    SeekBar(linearLayout, featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3]));
                    break;
                case "Button":
                    Button(linearLayout, featNum, strSplit[1]);
                    break;
                case "ButtonOnOff":
                    ButtonOnOff(linearLayout, featNum, strSplit[1], switchedOn);
                    break;
                case "Spinner":
                    TextView(linearLayout, strSplit[1]);
                    Spinner(linearLayout, featNum, strSplit[1], strSplit[2]);
                    break;
                case "InputText":
                    InputText(linearLayout, featNum, strSplit[1]);
                    break;
                case "InputValue":
                    if (strSplit.length == 3)
                        InputNum(linearLayout, featNum, strSplit[2], Integer.parseInt(strSplit[1]));
                    if (strSplit.length == 2)
                        InputNum(linearLayout, featNum, strSplit[1], 0);
                    break;
                case "InputLValue":
                    if (strSplit.length == 3)
                        InputLNum(linearLayout, featNum, strSplit[2], Long.parseLong(strSplit[1]));
                    if (strSplit.length == 2)
                        InputLNum(linearLayout, featNum, strSplit[1], 0);
                    break;
                case "CheckBox":
                    CheckBox(linearLayout, featNum, strSplit[1], switchedOn);
                    break;
                case "RadioButton":
                    RadioButton(linearLayout, featNum, strSplit[1], strSplit[2]);
                    break;
                case "Collapse":
                    Collapse(linearLayout, strSplit[1], switchedOn);
                    subFeat++;
                    break;
                case "ButtonLink":
                    subFeat++;
                    ButtonLink(linearLayout, strSplit[1], strSplit[2]);
                    break;
                case "Category":
                    subFeat++;
                    Category(linearLayout, strSplit[1]);
                    break;
                case "RichTextView":
                    subFeat++;
                    TextView(linearLayout, strSplit[1]);
                    break;
                case "RichWebView":
                    subFeat++;
                    WebTextView(linearLayout, strSplit[1]);
                    break;
            }
        }
    }

    private void Switch(LinearLayout linLayout, final int featNum, final String featName, boolean swiOn) {
        LinearLayout row = new LinearLayout(getContext);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(40, 20, 60, 20);

        TextView txt = new TextView(getContext);
        txt.setText(Html.fromHtml(featName)); 
        txt.setTextColor(TEXT_COLOR);
        txt.setTextSize(14f);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f);
        txt.setLayoutParams(txtParams);

        final Switch switchR = new Switch(getContext);
        ColorStateList thumbStates = new ColorStateList(
            new int[][]{ new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_checked}, new int[]{} },
            new int[]{ Color.LTGRAY, Color.WHITE, Color.WHITE }
        );
        ColorStateList trackStates = new ColorStateList(
            new int[][]{ new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_checked}, new int[]{} },
            new int[]{ Color.GRAY, SWITCH_ON_COLOR, ToggleOFF }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switchR.setThumbTintList(thumbStates);
            switchR.setTrackTintList(trackStates);
        }

        switchR.setChecked(Preferences.loadPrefBool(featName, featNum, swiOn));
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
                    vibrate(15); 
                    Preferences.changeFeatureBool(featName, featNum, bool);
                    if (featNum >= 0) {
                        String status = bool ? "Enabled" : "Disabled";
                        String cleanName = Html.fromHtml(featName).toString();
                        showCustomToast(cleanName + " " + status);
                    }
                    switch (featNum) {
                        case -1:
                            Preferences.with(switchR.getContext()).writeBoolean(-1, bool);
                            if (!bool) Preferences.with(switchR.getContext()).clear();
                            break;
                        case -3:
                            Preferences.isExpanded = bool;
                            scrollView.setLayoutParams(bool ? scrlLLExpanded : scrlLL);
                            break;
                    }
                }
            });

        row.addView(txt);
        row.addView(switchR);

        View line = new View(getContext);
        line.setBackgroundColor(SEPARATOR_COLOR);
        line.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 1));
        ((ViewGroup.MarginLayoutParams) line.getLayoutParams()).setMargins(40, 0, 40, 0);

        linLayout.addView(row);
        linLayout.addView(line);
    }

    private void SeekBar(LinearLayout linLayout, final int featNum, final String featName, final int min, int max) {
        LinearLayout container = new LinearLayout(getContext);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 10);

        int loadedProg = Preferences.loadPrefInt(featName, featNum);
        final TextView txt = new TextView(getContext);
        txt.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'><b>" + ((loadedProg == 0) ? min : loadedProg) + "</b></font>"));
        txt.setTextColor(TEXT_COLOR);
        txt.setTextSize(13f);

        SeekBar seekBar = new SeekBar(getContext);
        // FIX: Padding ditambah (30px) agar bulatannya tidak kepotong saat di ujung
        seekBar.setPadding(30, 10, 30, 10); 

        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) seekBar.setMin(min);
        seekBar.setProgress((loadedProg == 0) ? min : loadedProg);

        seekBar.getThumb().setColorFilter(SeekBarColor, PorterDuff.Mode.SRC_ATOP);
        seekBar.getProgressDrawable().setColorFilter(SeekBarProgressColor, PorterDuff.Mode.SRC_ATOP);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    int val = i < min ? min : i;
                    seekBar.setProgress(val);
                    Preferences.changeFeatureInt(featName, featNum, val);
                    txt.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'><b>" + val + "</b></font>"));
                }
            });

        container.addView(txt);
        container.addView(seekBar);

        View line = new View(getContext);
        line.setBackgroundColor(SEPARATOR_COLOR);
        line.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 1));
        ((ViewGroup.MarginLayoutParams) line.getLayoutParams()).setMargins(40, 0, 40, 0);

        linLayout.addView(container);
        linLayout.addView(line);
    }

    private void Button(LinearLayout linLayout, final int featNum, final String featName) {
        Button btn = new Button(getContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.setMargins(40, 15, 40, 15);
        btn.setLayoutParams(params);
        btn.setText(Html.fromHtml(featName)); 
        btn.setTextColor(ACCENT_COLOR);
        btn.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        btn.setBackground(bg);

        btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vibrate(20);
                    switch (featNum) {
                        case -6: 
                            scrollView.removeView(mSettings);
                            scrollView.addView(mods);
                            break;
                        case -100: 
                            stopChecking = true; 
                            break;
                    }
                    Preferences.changeFeatureInt(featName, featNum, 0);
                }
            });
        linLayout.addView(btn);
    }

    private void ButtonLink(LinearLayout linLayout, final String featName, final String url) {
        Button btn = new Button(getContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.setMargins(40, 15, 40, 15);
        btn.setLayoutParams(params);
        btn.setText(Html.fromHtml(featName)); 
        btn.setTextColor(ACCENT_COLOR);
        btn.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        btn.setBackground(bg);
        btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vibrate(20);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                    getContext.startActivity(intent);
                }
            });
        linLayout.addView(btn);
    }

    private void ButtonOnOff(LinearLayout linLayout, final int featNum, String featName, boolean switchedOn) {
        final Button button = new Button(getContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.setMargins(40, 15, 40, 15);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR);
        button.setAllCaps(false); //Disable caps to support html

        final String finalfeatName = featName.replace("OnOff_", "");
        boolean isOn = Preferences.loadPrefBool(featName, featNum, switchedOn);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(15);

        if (isOn) {
            button.setText(Html.fromHtml(finalfeatName + ": ON"));
            bg.setColor(BtnON);
            isOn = false;
        } else {
            button.setText(Html.fromHtml(finalfeatName + ": OFF"));
            bg.setColor(BtnOFF);
            isOn = true;
        }
        button.setBackground(bg);

        final boolean finalIsOn = isOn;
        button.setOnClickListener(new View.OnClickListener() {
                boolean isOn = finalIsOn;

                public void onClick(View v) {
                    vibrate(20);
                    Preferences.changeFeatureBool(finalfeatName, featNum, isOn);

                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(15);
                    if (isOn) {
                        button.setText(Html.fromHtml(finalfeatName + ": ON"));
                        bg.setColor(BtnON);
                        isOn = false;
                    } else {
                        button.setText(Html.fromHtml(finalfeatName + ": OFF"));
                        bg.setColor(BtnOFF);
                        isOn = true;
                    }
                    button.setBackground(bg);
                }
            });
        linLayout.addView(button);
    }

    private void Spinner(LinearLayout linLayout, final int featNum, final String featName, final String list) {
        Log.d(TAG, "spinner " + featNum + " " + featName + " " + list);
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        // Create another LinearLayout as a workaround to use it as a background
        // to keep the down arrow symbol. No arrow symbol if setBackgroundColor set
        LinearLayout linearLayout2 = new LinearLayout(getContext);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams2.setMargins(40, 15, 40, 15);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        linearLayout2.setBackground(bg);
        linearLayout2.setLayoutParams(layoutParams2);

        final Spinner spinner = new Spinner(getContext, Spinner.MODE_DROPDOWN);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP); //trick to show white down arrow color
        //Creating the ArrayAdapter instance having the list
        ArrayAdapter aa = new ArrayAdapter(getContext, android.R.layout.simple_spinner_dropdown_item, lists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner'
        spinner.setAdapter(aa);
        spinner.setSelection(Preferences.loadPrefInt(featName, featNum));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    Preferences.changeFeatureInt(spinner.getSelectedItem().toString(), featNum, position);
                    ((TextView) parentView.getChildAt(0)).setTextColor(TEXT_COLOR);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        linearLayout2.addView(spinner);
        linLayout.addView(linearLayout2);
    }

    private void InputNum(LinearLayout linLayout, final int featNum, final String featName, final int maxValue) {
        final Button button = new Button(getContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.setMargins(40, 15, 40, 15);
        button.setLayoutParams(layoutParams);

        int num = Preferences.loadPrefInt(featName, featNum);
        button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        button.setBackground(bg);
        button.setTextColor(TEXT_COLOR);

        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vibrate(20);
                    AlertDialog.Builder alertName = new AlertDialog.Builder(getContext);
                    final EditText editText = new EditText(getContext);
                    if (maxValue != 0)
                        editText.setHint("Max value: " + maxValue);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(10);
                    editText.setFilters(FilterArray);
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                if (hasFocus) {
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                } else {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }
                            }
                        });
                    editText.requestFocus();

                    alertName.setTitle("Input number");
                    alertName.setView(editText);
                    LinearLayout layoutName = new LinearLayout(getContext);
                    layoutName.setOrientation(LinearLayout.VERTICAL);
                    layoutName.addView(editText); // displays the user input bar
                    alertName.setView(layoutName);

                    alertName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int num;
                                try {
                                    String inp = editText.getText().toString();
                                    num = Integer.parseInt(inp.isEmpty() ? "0" : inp);
                                    if (maxValue != 0 && num >= maxValue)
                                        num = maxValue;
                                } catch (NumberFormatException ex) {
                                    if (maxValue != 0)
                                        num = maxValue;
                                    else
                                        num = Integer.MAX_VALUE;
                                }

                                button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
                                Preferences.changeFeatureInt(featName, featNum, num);
                                editText.setFocusable(false);
                            }
                        });

                    alertName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // dialog.cancel(); // closes dialog
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });

                    if (overlayRequired) {
                        AlertDialog dialog = alertName.create(); // display the dialog
                        dialog.getWindow().setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
                        dialog.show();
                    } else {
                        alertName.show();
                    }
                }
            });

        linLayout.addView(button);
    }

    private void InputLNum(LinearLayout linLayout, final int featNum, final String featName, final long maxValue) {
        final Button button = new Button(getContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.setMargins(40, 15, 40, 15);
        button.setLayoutParams(layoutParams);

        long num = Preferences.loadPrefLong(featName, featNum);
        button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        button.setBackground(bg);
        button.setTextColor(TEXT_COLOR);

        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vibrate(20);
                    AlertDialog.Builder alertName = new AlertDialog.Builder(getContext);
                    final EditText editText = new EditText(getContext);
                    if (maxValue != 0)
                        editText.setHint("Max value: " + maxValue);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(20);
                    editText.setFilters(FilterArray);
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                if (hasFocus) {
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                } else {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }
                            }
                        });
                    editText.requestFocus();

                    alertName.setTitle("Input number");
                    alertName.setView(editText);
                    LinearLayout layoutName = new LinearLayout(getContext);
                    layoutName.setOrientation(LinearLayout.VERTICAL);
                    layoutName.addView(editText); // displays the user input bar
                    alertName.setView(layoutName);

                    alertName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                long num;
                                try {
                                    String inp = editText.getText().toString();
                                    num = Long.parseLong(inp.isEmpty() ? "0" : inp);
                                    if (maxValue != 0 && num >= maxValue)
                                        num = maxValue;
                                } catch (NumberFormatException ex) {
                                    if (maxValue != 0)
                                        num = maxValue;
                                    else
                                        num = Long.MAX_VALUE;
                                }

                                button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
                                Preferences.changeFeatureLong(featName, featNum, num);

                                editText.setFocusable(false);
                            }
                        });

                    alertName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // dialog.cancel(); // closes dialog
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });

                    if (overlayRequired) {
                        AlertDialog dialog = alertName.create(); // display the dialog
                        dialog.getWindow().setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
                        dialog.show();
                    } else {
                        alertName.show();
                    }
                }
            });

        linLayout.addView(button);
    }

    private void InputText(LinearLayout linLayout, final int featNum, final String featName) {
        final Button button = new Button(getContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.setMargins(40, 15, 40, 15);
        button.setLayoutParams(layoutParams);

        String string = Preferences.loadPrefString(featName, featNum);
        button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + string + "</font>"));
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BTN_COLOR);
        bg.setCornerRadius(15);
        button.setBackground(bg);
        button.setTextColor(TEXT_COLOR);

        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vibrate(20);
                    AlertDialog.Builder alertName = new AlertDialog.Builder(getContext);

                    final EditText editText = new EditText(getContext);
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                if (hasFocus) {
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                } else {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }
                            }
                        });
                    editText.requestFocus();

                    alertName.setTitle("Input text");
                    alertName.setView(editText);
                    LinearLayout layoutName = new LinearLayout(getContext);
                    layoutName.setOrientation(LinearLayout.VERTICAL);
                    layoutName.addView(editText); // displays the user input bar
                    alertName.setView(layoutName);

                    alertName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String str = editText.getText().toString();
                                button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + str + "</font>"));
                                Preferences.changeFeatureString(featName, featNum, str);
                                editText.setFocusable(false);
                            }
                        });

                    alertName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //dialog.cancel(); // closes dialog
                                InputMethodManager imm = (InputMethodManager) getContext.getSystemService(getContext.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });


                    if (overlayRequired) {
                        AlertDialog dialog = alertName.create(); // display the dialog
                        dialog.getWindow().setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
                        dialog.show();
                    } else {
                        alertName.show();
                    }
                }
            });

        linLayout.addView(button);
    }

    private void CheckBox(LinearLayout linLayout, final int featNum, final String featName, boolean switchedOn) {
        final CheckBox checkBox = new CheckBox(getContext);
        checkBox.setText(featName);
        checkBox.setTextColor(TEXT_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            checkBox.setButtonTintList(ColorStateList.valueOf(CheckBoxColor));
        checkBox.setChecked(Preferences.loadPrefBool(featName, featNum, switchedOn));
        checkBox.setPadding(40, 20, 40, 20);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    vibrate(15);
                    Preferences.changeFeatureBool(featName, featNum, isChecked);
                }
            });
        linLayout.addView(checkBox);
    }

    private void RadioButton(LinearLayout linLayout, final int featNum, String featName, final String list) {
        //Credit: LoraZalora
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        final TextView textView = new TextView(getContext);
        textView.setText(featName + ":");
        textView.setTextColor(TEXT_COLOR);
        textView.setPadding(40, 20, 40, 10);

        final RadioGroup radioGroup = new RadioGroup(getContext);
        radioGroup.setPadding(40, 5, 40, 10);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.addView(textView);

        for (int i = 0; i < lists.size(); i++) {
            final RadioButton Radioo = new RadioButton(getContext);
            final String finalfeatName = featName, radioName = lists.get(i);
            View.OnClickListener first_radio_listener = new View.OnClickListener() {
                public void onClick(View v) {
                    vibrate(15);
                    textView.setText(Html.fromHtml(finalfeatName + ": <font color='" + NumberTxtColor + "'>" + radioName));
                    Preferences.changeFeatureInt(finalfeatName, featNum, radioGroup.indexOfChild(Radioo));
                }
            };
            Radioo.setText(lists.get(i));
            Radioo.setTextColor(TEXT_COLOR_2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Radioo.setButtonTintList(ColorStateList.valueOf(RadioColor));
            Radioo.setOnClickListener(first_radio_listener);
            radioGroup.addView(Radioo);
        }

        int index = Preferences.loadPrefInt(featName, featNum);
        if (index > 0) { //Preventing it to get an index less than 1. below 1 = null = crash
            textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + lists.get(index - 1)));
            ((RadioButton) radioGroup.getChildAt(index)).setChecked(true);
        }
        linLayout.addView(radioGroup);
    }

    private void Collapse(LinearLayout linLayout, final String text, final boolean expanded) {
        LinearLayout.LayoutParams layoutParamsLL = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParamsLL.setMargins(0, 5, 0, 0);

        LinearLayout collapse = new LinearLayout(getContext);
        collapse.setLayoutParams(layoutParamsLL);
        collapse.setVerticalGravity(16);
        collapse.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout collapseSub = new LinearLayout(getContext);
        collapseSub.setVerticalGravity(16);
        collapseSub.setPadding(0, 5, 0, 5);
        collapseSub.setOrientation(LinearLayout.VERTICAL);
        collapseSub.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        collapseSub.setVisibility(View.GONE);
        mCollapse = collapseSub;

        final TextView textView = new TextView(getContext);
        textView.setBackgroundColor(CollapseColor);
        textView.setText("▽ " + text + " ▽");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 20);

        if (expanded) {
            collapseSub.setVisibility(View.VISIBLE);
            textView.setText("△ " + text + " △");
        }

        textView.setOnClickListener(new View.OnClickListener() {
                boolean isChecked = expanded;

                @Override
                public void onClick(View v) {
                    vibrate(20);
                    boolean z = !isChecked;
                    isChecked = z;
                    if (z) {
                        collapseSub.setVisibility(View.VISIBLE);
                        textView.setText("△ " + text + " △");
                        return;
                    }
                    collapseSub.setVisibility(View.GONE);
                    textView.setText("▽ " + text + " ▽");
                }
            });
        collapse.addView(textView);
        collapse.addView(collapseSub);
        linLayout.addView(collapse);
    }

    private void Category(LinearLayout linLayout, String text) {
        TextView txt = new TextView(getContext);
        txt.setText(Html.fromHtml(text).toString().toUpperCase()); 
        txt.setTextColor(TEXT_COLOR_2);
        txt.setTextSize(11f);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        txt.setPadding(40, 30, 40, 10);
        linLayout.addView(txt);
    }

    private void TextView(LinearLayout linLayout, String text) {
        TextView textView = new TextView(getContext);
        textView.setText(Html.fromHtml(text));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setPadding(40, 10, 40, 10);
        linLayout.addView(textView);
    }

    private void WebTextView(LinearLayout linLayout, String text) {
        WebView wView = new WebView(getContext);
        wView.loadData(text, "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setPadding(0, 5, 0, 5);
        wView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        linLayout.addView(wView);
    }

    // --- HELPER: HAPTIC FEEDBACK (SAFE MODE) ---
    private void vibrate(int ms) {
        try {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(ms, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(ms);
                }
            }
        } catch (Exception e) {
            // Ignore if permission missing
        }
    }

    // --- HELPER: CUSTOM TOAST ---
    private void showCustomToast(String message) {
        LinearLayout layout = new LinearLayout(getContext);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#CC1C1C1E")); 
        bg.setCornerRadius(50); 
        bg.setStroke(1, Color.parseColor("#33FFFFFF"));
        layout.setBackground(bg);
        layout.setPadding(40, 20, 40, 20);

        TextView text = new TextView(getContext);
        text.setText(Html.fromHtml("<font color='#ffffff'>" + message + "</font>"));
        text.setTextSize(13f);
        layout.addView(text);

        Toast toast = new Toast(getContext);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150); 
        toast.show();
    }

    private boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    //For our image a little converter
    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * getContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) i, getContext.getResources().getDisplayMetrics());
    }

    public void setVisibility(int view) {
        if (rootFrame != null) {
            rootFrame.setVisibility(view);
        }
    }

    public void onDestroy() {
        if (rootFrame != null) {
            mWindowManager.removeView(rootFrame);
        }
    }
}
