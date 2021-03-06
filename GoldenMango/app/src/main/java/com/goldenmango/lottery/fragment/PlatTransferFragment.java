package com.goldenmango.lottery.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.goldenmango.lottery.R;
import com.goldenmango.lottery.app.BaseFragment;
import com.goldenmango.lottery.app.GoldenMangoApp;
import com.goldenmango.lottery.base.net.GsonHelper;
import com.goldenmango.lottery.base.net.RestCallback;
import com.goldenmango.lottery.base.net.RestRequest;
import com.goldenmango.lottery.base.net.RestRequestManager;
import com.goldenmango.lottery.base.net.RestResponse;
import com.goldenmango.lottery.component.CustomDialog;
import com.goldenmango.lottery.component.DialogLayout;
import com.goldenmango.lottery.data.GetThirdPlatBalance;
import com.goldenmango.lottery.data.GetThirdPlatBalanceCommand;
import com.goldenmango.lottery.data.GetThirdPlatCommand;
import com.goldenmango.lottery.data.PlatForm;
import com.goldenmango.lottery.data.TransferFundsCommand;
import com.goldenmango.lottery.view.adapter.TransferAdapter;
import com.google.gson.reflect.TypeToken;
import com.michael.easydialog.EasyDialog;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ACE-PC on 2017/1/16.
 * 平台转账
 */

public class PlatTransferFragment extends BaseFragment {

    private static final int TRACE_GET_THIRD_RLAT = 1;
    private static final int TRACE_GET_THIRD_PLAT_BALANCE = 2;
    private static final int TRACE_POST_THIRD_SUBMIT = 3;


    @BindView(R.id.plat_type)
    LinearLayout platType;
    @BindView(R.id.plat_from)
    LinearLayout platFrom;
    @BindView(R.id.image_type)
    ImageView imageType;
    @BindView(R.id.image_plat)
    ImageView imageFrom;
    @BindView(R.id.transfer_amount)
    EditText transferAmount;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.plat_funds)
    TextView platFunds;
    @BindView(R.id.channel_funds)
    TextView channelFunds;
    @BindView(R.id.text_from)
    TextView textFrom;
    @BindView(R.id.text_to)
    TextView textTo;

    private ArrayList<PlatForm> platForm = new ArrayList<>();
    private Map<Integer, GetThirdPlatBalance> platBalanceMap = new HashMap<>();
    private int positionFromId = 0, positionFrom = 0;
    private int positionToId = 0, positionTo = 0;
    private boolean status = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, container, "频道转帐", R.layout.plat_transfer_fragment, true, true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //允许输入数字并且可以为小数
        transferAmount.setInputType(InputType.TYPE_CLASS_NUMBER );

    }

    /**
     * 初使化转帐平台数据
     */
    private void init() {
        if (platForm.size() == 0 && platBalanceMap.size() == 0) {
            showToast("无数据....");
            return;
        }
        if (platForm.size() >= 2) {
            PlatForm typePlat = platForm.get(1);
            positionFrom = 1;
            positionFromId = typePlat.getId();
            textFrom.setText(typePlat.getName());

            PlatForm plat = platForm.get(0);
            positionTo = 0;
            positionToId = plat.getId();
            textTo.setText(plat.getName());
            updatedUI();
        } else {
            CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
            builder.setMessage("资金转移平台不足!");
            builder.setTitle("温馨提示");
            builder.setLayoutSet(DialogLayout.SINGLE);
            builder.setPositiveButton("知道了", (dialog, which) -> {
                dialog.dismiss();
                getActivity().finish();
            });
            builder.create().show();
        }
    }

    private void updatedUI() {
        switch (positionFromId){
            case -1://平台
                platFunds.setText(platBalanceMap.get(4).getPlatAvailable());
                break;
            case 4://沙巴体育
                platFunds.setText(platBalanceMap.get(4).getBalance());
                break;
        }

        switch (positionToId){
            case -1://平台
                channelFunds.setText(platBalanceMap.get(4).getPlatAvailable());
                break;
            case 4://沙巴体育
                channelFunds.setText(platBalanceMap.get(4).getBalance());
                break;
        }


//        if (positionToId != -1) {
//            platFunds.setText(platBalanceMap.get(positionToId).getPlatAvailable());
//            channelFunds.setText(platBalanceMap.get(positionToId).getBalance());
//        } else if (positionFromId != -1) {
//            platFunds.setText(platBalanceMap.get(positionFromId).getPlatAvailable());
//            channelFunds.setText(platBalanceMap.get(positionFromId).getBalance());
//        }
    }

    private void showBubble(View locationView) {
        if (platForm.size() == 0 && platBalanceMap.size() == 0) {
            showToast("无数据....");
            return;
        }
        View view = getActivity().getLayoutInflater().inflate(R.layout.plat_info_view, null);

        TransferAdapter adapter = new TransferAdapter(getContext(), platForm, status ? positionFromId : positionToId);
        ListView platList = view.findViewById(R.id.plat_info_list);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) platList.getLayoutParams();
        linearParams.width = status ? platType.getWidth() : platFrom.getWidth();
        platList.setLayoutParams(linearParams);
        platList.setAdapter(adapter);

        EasyDialog easyDialog = new EasyDialog(getActivity())
                .setLayout(view)
                .setGravity(EasyDialog.GRAVITY_BOTTOM)
                .setBackgroundColor(getResources().getColor(R.color.background))
                .setLocationByAttachedView(locationView)
                .setAnimationTranslationShow(EasyDialog.DIRECTION_X, 350, 400, 100, -50, 50, 0)
                .setAnimationAlphaShow(350, 0.3f, 1.0f)
                .setAnimationTranslationDismiss(EasyDialog.DIRECTION_X, 350, -50, 400)
                .setAnimationAlphaDismiss(350, 1.0f, 0.0f)
                .setTouchOutsideDismiss(true)
                .setMatchParent(false)
                .setOutsideColor(getResources().getColor(R.color.halfTransparent))
                .show();

        adapter.setOnIssueNoClickListener((PlatForm plat, int position) -> {
            if (status) {
                positionFrom = position;
                positionFromId = plat.getId();
                textFrom.setText(plat.getName());
            } else {
                positionTo = position;
                positionToId = plat.getId();
                textTo.setText(plat.getName());
            }
            updatedUI();
            easyDialog.dismiss();
        });
    }

    @OnClick({R.id.plat_type, R.id.plat_from})
    public void selectDonw(View v) {
        if (platBalanceMap.size() == 0) {
            return ;
        }

        switch (v.getId()) {
            case R.id.plat_type:
                status = true;
                showBubble(platType);
                break;
            case R.id.plat_from:
                status = false;
                showBubble(imageFrom);
                break;
        }
    }

    //获取转帐平台
    private void GetThirdPlatLoad() {
        GetThirdPlatCommand routeCommand = new GetThirdPlatCommand();
        TypeToken typeToken = new TypeToken<RestResponse<ArrayList<PlatForm>>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), routeCommand, typeToken, restCallback, TRACE_GET_THIRD_RLAT, this);
        RestResponse restResponse = restRequest.getCache();
        if (restResponse != null && restResponse.getData() instanceof ArrayList<?>) {
            platForm = (ArrayList<PlatForm>) restResponse.getData();
        }
        restRequest.execute();
    }

    private void GetThirdPlatBalance(boolean status, int platId) {
        GetThirdPlatBalanceCommand routeCommand = new GetThirdPlatBalanceCommand();
        if (status) {
                    routeCommand.setPlatId(platId + "");
        }
        TypeToken typeToken = new TypeToken<RestResponse<Map<Integer, GetThirdPlatBalance>>>() {
        };
        RestRequest restRequest = RestRequestManager.createRequest(getActivity(), routeCommand, typeToken, restCallback, TRACE_GET_THIRD_PLAT_BALANCE, this);
        restRequest.execute();
    }

    @OnClick(R.id.btn_submit)
    public void btnSubmit() {
        if (TextUtils.isEmpty(transferAmount.getText())) {
            showToast("请输入转移金额", Toast.LENGTH_SHORT);
            return;
        }

        String  transferAmountStr=transferAmount.getText().toString();

        int drawMoney = Integer.parseInt(transferAmountStr);


        if (drawMoney < 10) {
            Toast.makeText(getActivity(), "转移金额不能小于10元", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (drawMoney > 100000) {
//            Toast.makeText(getActivity(), "转移金额不能大于100000元", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (platBalanceMap.size() == 0) {
            Toast.makeText(getActivity(), "暂无转帐平台", Toast.LENGTH_SHORT).show();
            return;
        }

        if (positionFromId == positionToId) {
            showToast("资金转移平台不能相同");
            return;
        }

        CustomDialog.Builder builder = new CustomDialog.Builder(getContext());
        builder.setMessage("确定要从" + platForm.get(positionFrom).getName() + "转账给" + platForm.get(positionTo).getName() + "吗？\n" + "本次转账金额：" + drawMoney + "元");
        builder.setTitle("转账确认");
        builder.setLayoutSet(DialogLayout.LEFT_AND_RIGHT);
//        builder.setNegativeBackground(R.drawable. notidialog_bottom_right_fillet_btn_s);
        builder.setNegativeButton(" 确认转帐", (dialog, which) -> {

            try {

                TransferFundsCommand fundsCommand = new TransferFundsCommand();
    //            fundsCommand.setToken(GoldenMangoApp.getUserCentre().getUserInfo().getToken());
                fundsCommand.setAmount(drawMoney);
                fundsCommand.setFromPlat(positionFromId);
                fundsCommand.setToPlat(positionToId);

                String requestCommand = GsonHelper.toJson(fundsCommand);
                requestCommand = requestCommand.replace(":", "=").replace(",", "&").replace("\"", "");

                fundsCommand.setSign(DigestUtils.md5Hex(URLEncoder.encode(requestCommand.substring(1, requestCommand.length() - 1) + "&packet=Fund&action=PlatTransfer", "UTF-8") + GoldenMangoApp.getUserCentre().getKeyApiKey()));

                TypeToken typeToken = new TypeToken<RestResponse<GetThirdPlatBalance>>() {
                };
                RestRequest restRequest = RestRequestManager.createRequest(getActivity(), fundsCommand, typeToken, restCallback, TRACE_POST_THIRD_SUBMIT, this);
                restRequest.execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            btnSubmit.setEnabled(false);
            dialog.dismiss();
        });
//        builder.setPositiveBackground(R.drawable.notidialog_bottom_left_fillet_btn_s);
        builder.setPositiveButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private RestCallback restCallback = new RestCallback() {
        @Override
        public boolean onRestComplete(RestRequest request, RestResponse response) {
            switch (request.getId()) {
                case TRACE_GET_THIRD_PLAT_BALANCE:
                    platBalanceMap = (Map<Integer, GetThirdPlatBalance>) response.getData();
                    if (platBalanceMap.size() > 0) {
                        init();
                    }
                    break;
                case TRACE_GET_THIRD_RLAT:
                    platForm = (ArrayList<PlatForm>) response.getData();
                    GetThirdPlatBalance(false, 0);
                    break;
                case TRACE_POST_THIRD_SUBMIT:
                    GetThirdPlatBalance getPlatBalance = (GetThirdPlatBalance) response.getData();
                    if (platBalanceMap.containsKey(getPlatBalance.getId())) {
                        platBalanceMap.put(getPlatBalance.getId(), getPlatBalance);
                        showToast(response.getError());
                    }
                    updatedUI();
                    break;
            }
            return true;
        }

        @Override
        public boolean onRestError(RestRequest request, int errCode, String errDesc) {
            if (errCode == 3004 || errCode == 2016) {
                signOutDialog(getActivity(), errCode);
                return true;
            } else {
                showToast(errDesc);
            }
            return false;
        }

        @Override
        public void onRestStateChanged(RestRequest request, @RestRequest.RestState int state) {
            if (state == RestRequest.RUNNING && (request.getId() == TRACE_GET_THIRD_RLAT || request.getId() == TRACE_GET_THIRD_PLAT_BALANCE)) {
//                dialogShow("进行中...");
            } else {
                btnSubmit.setEnabled(true);
//                dialogHide();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        GetThirdPlatLoad();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
