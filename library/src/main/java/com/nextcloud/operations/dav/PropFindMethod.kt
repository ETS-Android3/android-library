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

import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.DavResponseCallback
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.OkHttpMethodBase
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Webdav Propfind method that uses OkHttp with new NextcloudClient
 */
class PropFindMethod(
    val uri: String,
    val properties: ArrayList<Property.Name>,
    private val depth: Int
) : OkHttpDavMethodBase() {
    var davResponse: Response? = null

    override fun createDavResource(nextcloudClient: NextcloudClient) {
        val httpURL = uri.toHttpUrl()
        val davResource = DavResource(nextcloudClient.client, httpURL)
        val callback: DavResponseCallback = { response, _ ->
            davResponse = response
        }
        davResource.propfind(depth, *properties.toTypedArray(), callback = callback)
    }

    fun davResponse(): Response? {
        return davResponse
    }

    override fun statusCode(): Int {
        return davResponse?.status?.code ?: OkHttpMethodBase.UNKNOWN_STATUS_CODE
    }
}
