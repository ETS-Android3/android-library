/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.dav.PropFindMethod;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.util.ArrayList;

import at.bitfire.dav4jvm.Property;
import at.bitfire.dav4jvm.Response;
import at.bitfire.dav4jvm.exception.NotFoundException;
import at.bitfire.dav4jvm.property.GetETag;

/**
 * Check if file is up to date, by checking only eTag
 */
public class CheckEtagRemoteOperation extends RemoteOperation {
    private static final String TAG = CheckEtagRemoteOperation.class.getSimpleName();

    private String path;
    private String expectedEtag;

    public CheckEtagRemoteOperation(String path, String expectedEtag) {
        this.path = path;
        this.expectedEtag = expectedEtag;
    }


    @Override
    public RemoteOperationResult run(NextcloudClient client) {
        PropFindMethod propfind = null;

        try {
            ArrayList<Property.Name> propList = new ArrayList();
            propList.add(GetETag.NAME);

            propfind = new PropFindMethod(client.getFilesDavUri(path), propList, 0);
            client.execute(propfind);
            int status = propfind.statusCode();

            //if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
            Response resp = propfind.davResponse();

            String etag = WebdavUtils.parseEtag(resp.get(GetETag.class).getETag());

            if (etag.equals(expectedEtag)) {
                return new RemoteOperationResult(ResultCode.ETAG_UNCHANGED);
            } else {
                RemoteOperationResult result = new RemoteOperationResult(ResultCode.ETAG_CHANGED);

                ArrayList<Object> list = new ArrayList<>();
                list.add(etag);
                result.setData(list);

                return result;
            }
            //}
        } catch (Exception e) {
            if (e instanceof NotFoundException) {
                return new RemoteOperationResult(ResultCode.FILE_NOT_FOUND);
            }
            Log_OC.e(TAG, "Error while retrieving eTag");
        } finally {
            if (propfind != null) {
                propfind.releaseConnection();
            }
        }

        return new RemoteOperationResult(ResultCode.ETAG_CHANGED);
    }
}
