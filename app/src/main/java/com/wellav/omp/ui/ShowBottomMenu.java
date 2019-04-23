package com.wellav.omp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wellav.omp.R;
import com.wellav.omp.channel.Channel;
import com.wellav.omp.channel.Channels;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.iptv.HotelActivity;
import com.wellav.omp.iptv.LivePlayerActivity;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.ToastUtil;
import com.wellav.omp.utils.Utilss;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingjia.zheng on 2018/8/9.
 */

public class ShowBottomMenu implements View.OnClickListener {
    private final static int TO_HOME = 0;
    private final static int TO_TV = 1;
    private final static int TO_HOTEL = 2;
    private final static int TO_SERVICE = 3;
    private final static int TO_FOOD = 4;
    private final static int TOLIVEACTIVITY = 5;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private ViewGroup viewGroup;
    private int currentModel;

    private View inflate;

    private RelativeLayout relativeLayoutHome;
    private RelativeLayout relativeLayoutTV;
    private RelativeLayout relativeLayoutHotelItem;
    private RelativeLayout relativeLayoutServiceItem;
    private RelativeLayout relativeLayoutFoodItem;
    private LinearLayout linearLayoutBottom;

    private ImageView imageViewHome;
    private ImageView imageViewTV;
    private ImageView imageViewHotel;
    private ImageView imageViewService;
    private ImageView imageViewFood;

    private TextView textViewHome;
    private TextView textViewTV;
    private TextView textViewHotel;
    private TextView textViewService;
    private TextView textViewFood;
    private TextView hotelIntroduction;
    private TextView roomIntroduction;
    private TextView brandHistory;
    private TextView facilityIntroduction;
    private TextView businessServer;
    private TextView localFeatures;
    private TextView hotelFood;

    private String ipServer;
    private String data;
    private Channels mChannels;
    private boolean isMainActivity;

    public ShowBottomMenu(Context context, ViewGroup viewGroup, boolean isMainActivity) {
        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(context);
        editor = sharedPreferences.edit();
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
        currentModel = sharedPreferences.getInt(SharedData.CURRENTMODEL, 0);
        this.context = context;
        this.viewGroup = viewGroup;
        this.isMainActivity = isMainActivity;
        inflate = LayoutInflater.from(context).inflate(R.layout.bottom_menu, null);
        relativeLayoutHome = inflate.findViewById(R.id.Home);
        relativeLayoutTV = inflate.findViewById(R.id.TV);
        relativeLayoutHome.setOnClickListener(this);
        relativeLayoutTV.setOnClickListener(this);
        relativeLayoutHotelItem = inflate.findViewById(R.id.hotel_item);
        relativeLayoutServiceItem = inflate.findViewById(R.id.server_item);
        relativeLayoutFoodItem = inflate.findViewById(R.id.food_item);
        linearLayoutBottom = inflate.findViewById(R.id.linearLayout);
        linearLayoutBottom.setVisibility(View.GONE);
        imageViewHome = inflate.findViewById(R.id.imageHome);
        imageViewTV = inflate.findViewById(R.id.imageTV);
        imageViewHotel = inflate.findViewById(R.id.imageHotel);
        imageViewService = inflate.findViewById(R.id.imageService);
        imageViewFood = inflate.findViewById(R.id.imageFood);
        textViewHome = inflate.findViewById(R.id.textHome);
        textViewTV = inflate.findViewById(R.id.textTV);
        textViewHotel = inflate.findViewById(R.id.textHotel);
        textViewService = inflate.findViewById(R.id.textService);
        textViewFood = inflate.findViewById(R.id.textFood);

        hotelIntroduction = inflate.findViewById(R.id.hotel_introduction);
        roomIntroduction = inflate.findViewById(R.id.room_introduction);
        brandHistory = inflate.findViewById(R.id.brand_history);
        facilityIntroduction = inflate.findViewById(R.id.facility_introduction);
        businessServer = inflate.findViewById(R.id.business_server);
        hotelFood = inflate.findViewById(R.id.hotel_food);
        localFeatures = inflate.findViewById(R.id.local_features);

        hotelIntroduction.setOnClickListener(this);
        roomIntroduction.setOnClickListener(this);
        brandHistory.setOnClickListener(this);
        facilityIntroduction.setOnClickListener(this);
        businessServer.setOnClickListener(this);
        hotelFood.setOnClickListener(this);
        localFeatures.setOnClickListener(this);

        textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S18));
        textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S18));
        textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S18));
        textViewService.setTextSize(context.getResources().getDimension(R.dimen.S18));
        textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S18));
    }

    public void addView() {
        viewGroup.addView(inflate);
    }

    public void showBottom() {
        if (linearLayoutBottom.getVisibility() != View.VISIBLE) {
            linearLayoutBottom.setVisibility(View.VISIBLE);
            linearLayoutBottom.setAnimation(Utilss.moveToViewLocation());
            handler.sendEmptyMessageDelayed(0, 200);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    setNavBg(currentModel);
                    break;
                case 1:
                    linearLayoutBottom.setVisibility(View.INVISIBLE);
                    linearLayoutBottom.setAnimation(Utilss.moveToViewBottom());
                    break;
                case 2:
                    toHotel(false, SharedData.HOTELINTRODUCTION);
                    break;
                case 3:
                    toHotel(false, SharedData.BRANDHISTORY);
                    break;
                case 4:
                    toHotel(true, SharedData.ROOMINTRODUCTION);
                    break;
                case 5:
                    toHotel(false, SharedData.FACILITYINTRODUCTION);
                    break;
                case 6:
                    toHotel(false, SharedData.BUSINESSSERVICES);
                    break;
                case 7:
                    toHotel(false, SharedData.HOTELFOOD);
                    break;
                case 8:
                    toHotel(false, SharedData.lOCALFEATURES);
                    break;
            }
        }
    };

    public void hideBottom() {
        if (relativeLayoutHotelItem.getVisibility() == View.VISIBLE) {
            relativeLayoutHotelItem.setVisibility(View.INVISIBLE);
            relativeLayoutHotelItem.setAnimation(Utilss.moveToViewBottom());
            handler.sendEmptyMessageDelayed(1, 200);
        } else if (relativeLayoutServiceItem.getVisibility() == View.VISIBLE) {
            relativeLayoutServiceItem.setVisibility(View.INVISIBLE);
            relativeLayoutServiceItem.setAnimation(Utilss.moveToViewBottom());
            handler.sendEmptyMessageDelayed(1, 200);
        } else if (relativeLayoutFoodItem.getVisibility() == View.VISIBLE) {
            relativeLayoutFoodItem.setVisibility(View.INVISIBLE);
            relativeLayoutFoodItem.setAnimation(Utilss.moveToViewBottom());
            handler.sendEmptyMessageDelayed(1, 200);
        } else {
            linearLayoutBottom.setVisibility(View.INVISIBLE);
            linearLayoutBottom.setAnimation(Utilss.moveToViewBottom());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Home:
                if (!isMainActivity) {
                    ((Activity) context).finish();
                }
                break;
            case R.id.TV:
                toTV();
                break;
            case R.id.hotel_introduction:
                setToHotel(2);
                break;
            case R.id.brand_history:
                setToHotel(3);
                break;
            case R.id.room_introduction:
                setToHotel(4);
                break;
            case R.id.facility_introduction:
                setToHotel(5);
                break;
            case R.id.business_server:
                setToHotel(6);
                break;
            case R.id.hotel_food:
                setToHotel(7);
                break;
            case R.id.local_features:
                setToHotel(8);
                break;
        }

    }

    public void setToHotel(int i) {
        handler.sendEmptyMessage(i);
    }

    public void moveToRight() {
        currentModel = (currentModel + 1) % 5;
        setNavBg(currentModel);
    }

    public void moveToLeft() {
        currentModel = (currentModel + 4) % 5;
        setNavBg(currentModel);
    }

    public int getCurrentModel() {
        return currentModel;
    }


    public void toHotel(boolean isVideo, String title) {

        Intent hotelIntent = new Intent(context, HotelActivity.class);
        hotelIntent.putExtra("ipServer", ipServer);
        hotelIntent.putExtra("isVideo", isVideo);
        hotelIntent.putExtra("title", title);
        if (!isMainActivity) {
            hotelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ((Activity) context).finish();
        }
        context.startActivity(hotelIntent);
    }

    public void toTV() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                data = Utilss.httpGetChannels(ipServer + "/source/programlist.json");
                if ("".equals(data) || data == null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showText(context, "No signal! please check you network!");
                        }
                    });
                } else {
                    mHandler.sendEmptyMessage(TOLIVEACTIVITY);
                }
            }
        }).start();
    }

    public void updateIP() {
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TOLIVEACTIVITY:
                    try {
                        //节目列表
                        mChannels = new Channels();
                        JSONObject channelsJson = new JSONObject(data);
                        int code = Utilss.getJsonDataInt(channelsJson, "code");
                        if (code == 0) {
                            JSONObject dataJson = Utilss.getJsonObject(channelsJson, "data");
                            if (dataJson != null) {
                                JSONArray contentListJson = Utilss.getJsonArray(dataJson, "programlist");
                                if (contentListJson != null) {
                                    List<Channel> contentList = new ArrayList<Channel>();
                                    for (int i = 0; i < contentListJson.length(); i++) {
                                        JSONObject channel = contentListJson.getJSONObject(i);
                                        Channel c = new Channel();
                                        c.setType(Utilss.getJsonDataString(channel, "type"));
                                        c.setName(Utilss.getJsonDataString(channel, "name"));
                                        c.setPlayUrl(ipServer + Utilss.getJsonDataString(channel, "playUrl"));
                                        c.setPic(Utilss.getJsonDataString(channel, "pic"));
                                        contentList.add(c);
                                    }
                                    mChannels.setContentList(contentList);

                                    Intent inte = new Intent(context, LivePlayerActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("channels", mChannels);
                                    inte.putExtras(bundle);
                                    if (!isMainActivity) {
                                        ((Activity) context).finish();
                                        inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }
                                    context.startActivity(inte);

                                } else {
                                    Toast.makeText(context, context.getResources().getString(R.string.no_live_channel), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, context.getResources().getString(R.string.no_live_channel), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.no_live_channel), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {

                    }
                    break;
            }
        }
    };

    public void setNavBg(int setCurrentModel) {
        editor.putInt(SharedData.CURRENTMODEL, setCurrentModel);
        editor.commit();
        switch (setCurrentModel) {
            case TO_HOME:
                if (relativeLayoutFoodItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutFoodItem.setVisibility(View.INVISIBLE);
                    relativeLayoutFoodItem.setAnimation(Utilss.moveToViewBottom());
                }
                imageViewTV.setBackgroundResource(R.mipmap.tv);
                imageViewFood.setBackgroundResource(R.mipmap.food);
                textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewTV.setTextColor(context.getResources().getColor(R.color.white));
                textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewFood.setTextColor(context.getResources().getColor(R.color.white));
                relativeLayoutTV.setFocusable(false);
                hotelFood.setFocusable(false);
                imageViewHome.setBackgroundResource(R.mipmap.home_select);
                textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewHome.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutHome.setFocusable(true);
                relativeLayoutHome.requestFocus();
                break;
            case TO_TV:
                if (relativeLayoutHotelItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutHotelItem.setVisibility(View.INVISIBLE);
                    relativeLayoutHotelItem.setAnimation(Utilss.moveToViewBottom());
                }
                imageViewHome.setBackgroundResource(R.mipmap.home);
                textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewHome.setTextColor(context.getResources().getColor(R.color.white));
                imageViewHotel.setBackgroundResource(R.mipmap.hotel);
                textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewHotel.setTextColor(context.getResources().getColor(R.color.white));

                relativeLayoutHome.setFocusable(false);
                imageViewTV.setBackgroundResource(R.mipmap.tv_select);
                textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewTV.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutTV.setFocusable(true);
                relativeLayoutTV.requestFocus();
                break;
            case TO_HOTEL:
                if (relativeLayoutServiceItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutServiceItem.setVisibility(View.INVISIBLE);
                    relativeLayoutServiceItem.setAnimation(Utilss.moveToViewBottom());
                }
                imageViewTV.setBackgroundResource(R.mipmap.tv);
                textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewTV.setTextColor(context.getResources().getColor(R.color.white));
                imageViewService.setBackgroundResource(R.mipmap.server);
                textViewService.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewService.setTextColor(context.getResources().getColor(R.color.white));

                relativeLayoutTV.setFocusable(false);
                imageViewHotel.setBackgroundResource(R.mipmap.hotel_select);
                textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewHotel.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutHotelItem.setVisibility(View.VISIBLE);
                relativeLayoutHotelItem.setAnimation(Utilss.moveToViewLocation());
                hotelIntroduction.requestFocus();
                break;
            case TO_SERVICE:
                if (relativeLayoutHotelItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutHotelItem.setVisibility(View.INVISIBLE);
                    relativeLayoutHotelItem.setAnimation(Utilss.moveToViewBottom());
                }
                if (relativeLayoutFoodItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutFoodItem.setVisibility(View.INVISIBLE);
                    relativeLayoutFoodItem.setAnimation(Utilss.moveToViewBottom());
                }
                imageViewHotel.setBackgroundResource(R.mipmap.hotel);
                textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewHotel.setTextColor(context.getResources().getColor(R.color.white));
                imageViewFood.setBackgroundResource(R.mipmap.food);
                textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewFood.setTextColor(context.getResources().getColor(R.color.white));

                imageViewService.setBackgroundResource(R.mipmap.server_select);
                textViewService.setTextSize(context.getResources().getDimension(R.dimen.S20));
                textViewService.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutServiceItem.setVisibility(View.VISIBLE);
                relativeLayoutServiceItem.setAnimation(Utilss.moveToViewLocation());
                roomIntroduction.requestFocus();
                break;
            case TO_FOOD:
                if (relativeLayoutServiceItem.getVisibility() == View.VISIBLE) {
                    relativeLayoutServiceItem.setVisibility(View.INVISIBLE);
                    relativeLayoutServiceItem.setAnimation(Utilss.moveToViewBottom());
                }
                imageViewHome.setBackgroundResource(R.mipmap.home);
                textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewHome.setTextColor(context.getResources().getColor(R.color.white));
                imageViewService.setBackgroundResource(R.mipmap.server);
                textViewService.setTextSize(context.getResources().getDimension(R.dimen.S18));
                textViewService.setTextColor(context.getResources().getColor(R.color.white));

                relativeLayoutHome.setFocusable(false);
                imageViewFood.setBackgroundResource(R.mipmap.food_select);
                textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewFood.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutFoodItem.setVisibility(View.VISIBLE);
                relativeLayoutFoodItem.setAnimation(Utilss.moveToViewLocation());
                hotelFood.setFocusable(true);
                hotelFood.requestFocus();
                break;
        }
    }

    public void reSetNavBg(int newCurrnentModel) {
        if (currentModel == TO_HOME) {
            imageViewHome.setBackgroundResource(R.mipmap.home);
            textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S18));
            textViewHome.setTextColor(context.getResources().getColor(R.color.white));
            relativeLayoutHome.setFocusable(false);
        } else if (currentModel == TO_TV) {
            imageViewTV.setBackgroundResource(R.mipmap.tv);
            textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S18));
            textViewTV.setTextColor(context.getResources().getColor(R.color.white));
            relativeLayoutTV.setFocusable(false);
        } else if (currentModel == TO_HOTEL) {
            imageViewHotel.setBackgroundResource(R.mipmap.hotel);
            textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S18));
            textViewHotel.setTextColor(context.getResources().getColor(R.color.white));
            if (relativeLayoutHotelItem.getVisibility() == View.VISIBLE) {
                relativeLayoutHotelItem.setVisibility(View.INVISIBLE);
            }
            hotelFood.setFocusable(false);
        } else if (currentModel == TO_SERVICE) {
            imageViewService.setBackgroundResource(R.mipmap.server);
            textViewService.setTextSize(context.getResources().getDimension(R.dimen.S18));
            textViewService.setTextColor(context.getResources().getColor(R.color.white));
            if (relativeLayoutServiceItem.getVisibility() == View.VISIBLE) {
                relativeLayoutServiceItem.setVisibility(View.INVISIBLE);
            }
        } else if (currentModel == TO_FOOD) {
            imageViewFood.setBackgroundResource(R.mipmap.food);
            textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S18));
            textViewFood.setTextColor(context.getResources().getColor(R.color.white));
            if (relativeLayoutFoodItem.getVisibility() == View.VISIBLE) {
                relativeLayoutFoodItem.setVisibility(View.INVISIBLE);
            }
        }
        switch (newCurrnentModel) {
            case TO_HOME:
                imageViewHome.setBackgroundResource(R.mipmap.home_select);
                textViewHome.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewHome.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutHome.setFocusable(true);
                relativeLayoutHome.requestFocus();
                currentModel = TO_HOME;
                break;
            case TO_TV:
                imageViewTV.setBackgroundResource(R.mipmap.tv_select);
                textViewTV.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewTV.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutTV.setFocusable(true);
                relativeLayoutTV.requestFocus();
                currentModel = TO_TV;
                break;
            case TO_HOTEL:
                imageViewHotel.setBackgroundResource(R.mipmap.hotel_select);
                textViewHotel.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewHotel.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutHotelItem.setVisibility(View.VISIBLE);
                relativeLayoutHotelItem.setAnimation(Utilss.moveToViewLocation());
                hotelIntroduction.requestFocus();
                currentModel = TO_HOTEL;
                break;
            case TO_SERVICE:
                imageViewService.setBackgroundResource(R.mipmap.server_select);
                textViewService.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewService.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutServiceItem.setVisibility(View.VISIBLE);
                relativeLayoutServiceItem.setAnimation(Utilss.moveToViewLocation());
                roomIntroduction.requestFocus();
                currentModel = TO_SERVICE;
                break;
            case TO_FOOD:
                imageViewFood.setBackgroundResource(R.mipmap.food_select);
                textViewFood.setTextSize(context.getResources().getDimension(R.dimen.S19));
                textViewFood.setTextColor(context.getResources().getColor(R.color.blue));
                relativeLayoutFoodItem.setVisibility(View.VISIBLE);
                relativeLayoutFoodItem.setAnimation(Utilss.moveToViewLocation());
                hotelFood.setFocusable(true);
                hotelFood.requestFocus();
                currentModel = TO_FOOD;
                break;
        }
    }

    public void updateNavBg() {
        if (currentModel == TO_HOME) {
            imageViewHome.setBackgroundResource(R.mipmap.home);
            relativeLayoutHome.setFocusable(false);
        } else if (currentModel == TO_TV) {
            imageViewTV.setBackgroundResource(R.mipmap.tv);
            relativeLayoutTV.setFocusable(false);
        } else if (currentModel == TO_HOTEL) {
            imageViewHotel.setBackgroundResource(R.mipmap.hotel);
            hotelFood.setFocusable(false);
        } else if (currentModel == TO_SERVICE) {
            imageViewService.setBackgroundResource(R.mipmap.server);
        } else if (currentModel == TO_FOOD) {
            imageViewFood.setBackgroundResource(R.mipmap.food);
        }
    }
}
