// This file was generated by PermissionsDispatcher. Do not modify!
package com.goldenmango.lottery.fragment;

import java.lang.Override;
import java.lang.String;
import java.lang.ref.WeakReference;
import permissions.dispatcher.GrantableRequest;
import permissions.dispatcher.PermissionUtils;

final class HtmlFragmentPermissionsDispatcher {
  private static final int REQUEST_CALLCARD = 0;

  private static final String[] PERMISSION_CALLCARD = new String[] {"android.permission.READ_EXTERNAL_STORAGE"};

  private static GrantableRequest PENDING_CALLCARD;

  private HtmlFragmentPermissionsDispatcher() {
  }

  static void callCardWithCheck(HtmlFragment target, String path) {
    if (PermissionUtils.hasSelfPermissions(target.getActivity(), PERMISSION_CALLCARD)) {
      target.callCard(path);
    } else {
      PENDING_CALLCARD = new CallCardPermissionRequest(target, path);
      if (PermissionUtils.shouldShowRequestPermissionRationale(target.getActivity(), PERMISSION_CALLCARD)) {
        target.showCard(PENDING_CALLCARD);
      } else {
        target.requestPermissions(PERMISSION_CALLCARD, REQUEST_CALLCARD);
      }
    }
  }

  static void onRequestPermissionsResult(HtmlFragment target, int requestCode, int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CALLCARD:
      if (PermissionUtils.getTargetSdkVersion(target.getActivity()) < 23 && !PermissionUtils.hasSelfPermissions(target.getActivity(), PERMISSION_CALLCARD)) {
        target.onCardDenied();
        return;
      }
      if (PermissionUtils.verifyPermissions(grantResults)) {
        if (PENDING_CALLCARD != null) {
          PENDING_CALLCARD.grant();
        }
      } else {
        if (!PermissionUtils.shouldShowRequestPermissionRationale(target.getActivity(), PERMISSION_CALLCARD)) {
          target.onCardNeverAsk();
        } else {
          target.onCardDenied();
        }
      }
      PENDING_CALLCARD = null;
      break;
      default:
      break;
    }
  }

  private static final class CallCardPermissionRequest implements GrantableRequest {
    private final WeakReference<HtmlFragment> weakTarget;

    private final String path;

    private CallCardPermissionRequest(HtmlFragment target, String path) {
      this.weakTarget = new WeakReference<HtmlFragment>(target);
      this.path = path;
    }

    @Override
    public void proceed() {
      HtmlFragment target = weakTarget.get();
      if (target == null) return;
      target.requestPermissions(PERMISSION_CALLCARD, REQUEST_CALLCARD);
    }

    @Override
    public void cancel() {
      HtmlFragment target = weakTarget.get();
      if (target == null) return;
      target.onCardDenied();
    }

    @Override
    public void grant() {
      HtmlFragment target = weakTarget.get();
      if (target == null) return;
      target.callCard(path);
    }
  }
}
