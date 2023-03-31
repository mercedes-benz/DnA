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

package com.daimler.data.service.userwidgetpref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.UserWidgetPrefAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.UserWidgetPreferenceNsql;
import com.daimler.data.db.repo.userwidgetpref.UserWidgetPrefCustomRepository;
import com.daimler.data.db.repo.userwidgetpref.UserWidgetPrefRepository;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BaseUserWidgetPrefService extends
        BaseCommonService<UserWidgetPreferenceVO, UserWidgetPreferenceNsql, String> implements UserWidgetPrefService {


    private static Logger LOGGER = LoggerFactory.getLogger(BaseUserWidgetPrefService.class);

    public BaseUserWidgetPrefService() {
        super();
    }

    @Autowired
    public BaseUserWidgetPrefService(UserWidgetPrefCustomRepository customRepo, UserWidgetPrefRepository jpaRepo,
                                     UserWidgetPrefAssembler userWidgetPrefAssembler) {
        super(jpaRepo, userWidgetPrefAssembler, customRepo);
    }

    @Override
    @Transactional
    public ResponseEntity<UserWidgetPreferenceVO> createUserWidgetPreference(
            UserWidgetPreferenceVO requestUserWidgetPrefVO) {
        boolean requesteCreated = true;
        try {
            String userId = requestUserWidgetPrefVO.getUserId();
            UserWidgetPreferenceVO existingUserWidgetPrefVO = super.getByUniqueliteral("userId", userId);
            if (existingUserWidgetPrefVO != null && existingUserWidgetPrefVO.getUserId() != null) {
                requestUserWidgetPrefVO.setId(existingUserWidgetPrefVO.getId());
                requesteCreated = false;
            } else
                requestUserWidgetPrefVO.setId(null);
            UserWidgetPreferenceVO userWidgetPrefVO = super.create(requestUserWidgetPrefVO);
            if (userWidgetPrefVO != null && userWidgetPrefVO.getId() != null) {
                LOGGER.info("UserWidgetPreference created or updated successfully for userId {} ", userId);
                return new ResponseEntity<>(userWidgetPrefVO, requesteCreated ? HttpStatus.CREATED : HttpStatus.OK);
            } else {
                LOGGER.error("UserWidgetPreference failed to create for userId {} ", userId);
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred:{} while creating userWidgetPreference for userId {} ", e.getMessage(),
                    requestUserWidgetPrefVO.getUserId());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<GenericMessage> deleteUserWidgetPreference(String id) {
        try {
            super.deleteById(id);
            GenericMessage successMsg = new GenericMessage();
            successMsg.setSuccess("success");
            LOGGER.info("UserWidgetPref with id {} deleted successfully", id);
            return new ResponseEntity<>(successMsg, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Failed to delete userWidgetPref with id {} , due to internal error.", id);
            MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(exceptionMsg);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
