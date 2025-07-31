
/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.util;

import java.util.List;

import com.daimler.data.application.client.AuthoriserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FabricWorkspaceUtility {

    @Autowired
    private AuthoriserClient identityClient;

    public boolean hasProjectAdminAccess(String userId, String wsId) {
        List<String> allEntitList = identityClient.getAllUserEntitlements(userId);
        String entitlementName = wsId + "_Admin";
        return allEntitList.stream()
                .anyMatch(s -> s.toLowerCase().contains(entitlementName.toLowerCase()));
    }

    public String getUserRole(List<String> filteredEntitlements) {
        System.out.println("inside util________________________________"+filteredEntitlements);
        if (filteredEntitlements == null) {
            return ConstantsUtility.PERMISSION_VIEWER;
        }

        for (String entitlement : filteredEntitlements) {
            if (entitlement == null)
                continue;

            String lowerEntitlement = entitlement.toLowerCase();

            if (lowerEntitlement.endsWith("admin")) {
                return ConstantsUtility.PERMISSION_ADMIN;
            }
            if (lowerEntitlement.endsWith("contributor")) {
                return ConstantsUtility.PERMISSION_CONTRIBUTOR;
            }
            if (lowerEntitlement.endsWith("member")) {
                return ConstantsUtility.PERMISSION_MEMBER;
            }
        }
        return ConstantsUtility.PERMISSION_VIEWER;
    }

}
