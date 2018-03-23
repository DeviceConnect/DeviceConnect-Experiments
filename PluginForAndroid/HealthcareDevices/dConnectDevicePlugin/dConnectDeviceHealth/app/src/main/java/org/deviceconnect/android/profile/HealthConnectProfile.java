/*
 HealthConnectProfile
 Copyright (c) 2014-2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.HealthProfileConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health プロファイル.
 *
 * <p>
 * スマートデバイスに対しての健康機器操作機能を提供するAPI.<br/>
 * スマートデバイスに対しての健康機器操作機能を提供するデバイスプラグインはHealthConnectSubProfileクラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 *
 * <h1>各API提供メソッド</h1>
 * <p>
 * Health Profile の各APIへのリクエストに対し、HealthSubProfileを継承したサブProfileへの通知を行う。<br/>
 * ヘルスケア機器向けプロファイルとして本クラスを生成する際に、コンストラクタ内でサブクラスを生成しmSubProfileMapへ登録すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 */
public class HealthConnectProfile extends DConnectProfile implements HealthProfileConstants {

    public HealthConnectProfile() {
    }

    /**
     * サブプロファイルの追加
     * @param attribute サブプロファイル名。HTMLからの要求時に指定されたattributeと一致する場合に通知を受け取る。
     * @param subProfile HealthConnectSubProfileを継承したクラス参照
     */
    public void addSubProfile(String attribute, HealthConnectSubProfile subProfile) {
        mSubProfileMap.put(attribute, subProfile);
    }

    /**
     * HealthCareサブプロファイル
     */
    private final Map<String, HealthConnectSubProfile> mSubProfileMap = new ConcurrentHashMap<>();

    /**
     * HealthCareサブプロファイルのクラス定義
     * ヘルスケア用プロファイルを追加する場合は、HealthConnectSubProfileを基底クラスとして
     * それぞれのAPIをオーバーライドすること。
     */
    public static class HealthConnectSubProfile {
        protected boolean onGetRequest(final Intent request, final Intent response,
                                       final String serviceId, final String sessionKey) {
            setUnsupportedError(response);
            return true;
        }
        protected boolean onPutRequest(final Intent request, final Intent response,
                                       final String serviceId, final String sessionKey) {
            setUnsupportedError(response);
            return true;
        }
        protected boolean onDeleteRequest(final Intent request, final Intent response,
                                          final String serviceId, final String sessionKey) {
            setUnsupportedError(response);
            return true;
        }
        protected boolean onPostRequest(final Intent request, final Intent response,
                                        final String serviceId, final String sessionKey) {
            setUnsupportedError(response);
            return true;
        }
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        boolean result = true;

        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        String sessionKey = getSessionKey(request);

        HealthConnectSubProfile subProfile = mSubProfileMap.get(attribute);
        if (subProfile != null) {
            result = subProfile.onGetRequest(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;

        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        String sessionKey = getSessionKey(request);

        HealthConnectSubProfile subProfile = mSubProfileMap.get(attribute);
        if (subProfile != null) {
            result = subProfile.onPutRequest(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }
    
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;

        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        String sessionKey = getSessionKey(request);

        HealthConnectSubProfile subProfile = mSubProfileMap.get(attribute);
        if (subProfile != null) {
            result = subProfile.onDeleteRequest(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        boolean result = true;

        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        String sessionKey = getSessionKey(request);

        HealthConnectSubProfile subProfile = mSubProfileMap.get(attribute);
        if (subProfile != null) {
            result = subProfile.onPostRequest(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }
}
