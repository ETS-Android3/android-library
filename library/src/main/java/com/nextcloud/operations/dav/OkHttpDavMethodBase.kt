/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */

package com.nextcloud.operations.dav

import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.OkHttpMethodBase
import okhttp3.Response

abstract class OkHttpDavMethodBase {
    var response: Response? = null

    fun execute(nextcloudClient: NextcloudClient) {
        nextcloudClient.client = nextcloudClient.client
            .newBuilder()
            .followRedirects(false)
            .addInterceptor(BasicAuthInterceptor(nextcloudClient.credentials))
            .build()

        createDavResource(nextcloudClient)
    }

    abstract fun createDavResource(nextcloudClient: NextcloudClient)

    open fun statusCode(): Int {
        return response?.code ?: OkHttpMethodBase.UNKNOWN_STATUS_CODE
    }

    fun releaseConnection() {
        response?.body?.close()
    }

    fun succeeded(): Boolean {
        return response?.isSuccessful ?: false
    }
}
