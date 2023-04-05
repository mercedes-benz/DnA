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

package com.daimler.data.assembler;

import com.daimler.data.db.entities.UserWidgetPreferenceNsql;
import com.daimler.data.db.jsonb.userwidgetpref.*;
import com.daimler.data.dto.dataproductlov.AgileReleaseTrainVO;
import com.daimler.data.dto.dataproductlov.CarlaFunctionVO;
import com.daimler.data.dto.dataproductlov.FrontEndToolsVO;
import com.daimler.data.dto.dataproductlov.PlatformVO;
import com.daimler.data.dto.userwidgetpref.FilterPreferencesVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserWidgetPrefAssembler implements GenericAssembler<UserWidgetPreferenceVO, UserWidgetPreferenceNsql> {


    @Override
    public UserWidgetPreferenceVO toVo(UserWidgetPreferenceNsql entity) {
        UserWidgetPreferenceVO userWidgetPreferenceVO = null;
        if (Objects.nonNull(entity)) {
            userWidgetPreferenceVO = new UserWidgetPreferenceVO();
            UserWidgetPreference userWidgetPreference = entity.getData();
            userWidgetPreferenceVO.setId(entity.getId());
            if (userWidgetPreference != null) {
                userWidgetPreferenceVO.setUserId(userWidgetPreference.getUserId());

                FilterPreferences filterPrefs = userWidgetPreference.getFilterPreferences();
                FilterPreferencesVO filterPrefVO = new FilterPreferencesVO();
                if (filterPrefs != null) {
                    BeanUtils.copyProperties(filterPrefs, filterPrefVO);

                    if (filterPrefs.getCarlafunction() != null) {
                        List<CarlaFunctionVO> carlafunctionVo = new ArrayList<>();
                        carlafunctionVo = filterPrefs.getCarlafunction().stream().map(n -> this.toCarlafunctionVO(n)).collect(Collectors.toList());
                        filterPrefVO.setCarlafunction(carlafunctionVo);
                    }
                    if (filterPrefs.getPlatform() != null) {
                        List<PlatformVO> platforms = new ArrayList<>();;
                        platforms = filterPrefs.getPlatform().stream().map(n -> this.toPlatformnVO(n)).collect(Collectors.toList());
                        filterPrefVO.setPlatform(platforms);
                    }
                    if (filterPrefs.getAgileReleaseTrain() != null) {
                        List<AgileReleaseTrainVO> aggileReleaseTrain = new ArrayList<>();;
                        aggileReleaseTrain = filterPrefs.getAgileReleaseTrain().stream().map(n -> this.toAgileReleaseTrainVO(n)).collect(Collectors.toList());
                        filterPrefVO.setAgileReleaseTrain(aggileReleaseTrain);
                    }
                    if (filterPrefs.getFrontendTools() != null) {
                        List<FrontEndToolsVO> frontEndTools = new ArrayList<>();;
                        frontEndTools = filterPrefs.getFrontendTools().stream().map(n -> this.toFrontendToolsVO(n)).collect(Collectors.toList());
                        filterPrefVO.setFrontendTools(frontEndTools);
                    }

                }
                userWidgetPreferenceVO.setFilterPreferences(filterPrefVO);
            }
        }
        return userWidgetPreferenceVO;
    }

    public CarlaFunctionVO toCarlafunctionVO(WidgetUserCarlafunction widgetcarlaFunction) {
        CarlaFunctionVO carlafunction = new CarlaFunctionVO();
        if(widgetcarlaFunction!=null) {
            carlafunction.setId(widgetcarlaFunction.getId());
            carlafunction.setName(widgetcarlaFunction.getName());
        }
        return carlafunction;
    }

    public AgileReleaseTrainVO toAgileReleaseTrainVO(WidgetUserAgileReleaseTrain widgetAgileReleaseFunction) {
        AgileReleaseTrainVO agileReleaseTrain = new AgileReleaseTrainVO();
        if(widgetAgileReleaseFunction!=null) {
            agileReleaseTrain.setId(widgetAgileReleaseFunction.getId());
            agileReleaseTrain.setName(widgetAgileReleaseFunction.getName());
        }
        return agileReleaseTrain;
    }

    public FrontEndToolsVO toFrontendToolsVO(WidgetUserFrontEndTools widgetUserFrontEndTools) {
        FrontEndToolsVO frontEndToolsVO = new FrontEndToolsVO();
        if(widgetUserFrontEndTools!=null) {
            frontEndToolsVO.setId(widgetUserFrontEndTools.getId());
            frontEndToolsVO.setName(widgetUserFrontEndTools.getName());
        }
        return frontEndToolsVO;
    }

    public PlatformVO toPlatformnVO(WidgetUserPlatform widgetPlatform) {
        PlatformVO platform = new PlatformVO();
        if(widgetPlatform!=null) {
            platform.setId(widgetPlatform.getId());
            platform.setName(widgetPlatform.getName());
        }
        return platform;
    }


    public WidgetUserCarlafunction toWidgetCarlafunction(CarlaFunctionVO carlaFunctionVO) {
            WidgetUserCarlafunction carlafunction = new WidgetUserCarlafunction();
            if(carlaFunctionVO!=null) {
                carlafunction.setId(carlaFunctionVO.getId());
                carlafunction.setName(carlaFunctionVO.getName());
            }
        return carlafunction;
    }

    public WidgetUserAgileReleaseTrain toWidgetAgileReleaseTranin(AgileReleaseTrainVO agileReleaseTrainVO) {
        WidgetUserAgileReleaseTrain agileReleaseTrain = new WidgetUserAgileReleaseTrain();
        if(agileReleaseTrainVO!=null) {
            agileReleaseTrain.setId(agileReleaseTrainVO.getId());
            agileReleaseTrain.setName(agileReleaseTrainVO.getName());
        }
        return agileReleaseTrain;
    }

    public WidgetUserFrontEndTools toWidgetFrontEndTools(FrontEndToolsVO frontEndToolsVO) {
        WidgetUserFrontEndTools frontEndTools = new WidgetUserFrontEndTools();
        if(frontEndToolsVO!=null) {
            frontEndTools.setId(frontEndToolsVO.getId());
            frontEndTools.setName(frontEndToolsVO.getName());
        }
        return frontEndTools;
    }

    public WidgetUserPlatform toWidgetPlatform(PlatformVO platformVO) {
        WidgetUserPlatform platforms = new WidgetUserPlatform();
        if(platformVO!=null) {
            platforms.setId(platformVO.getId());
            platforms.setName(platformVO.getName());
        }
        return platforms;
    }

    @Override
    public UserWidgetPreferenceNsql toEntity(UserWidgetPreferenceVO vo) {
        UserWidgetPreferenceNsql userWidgetPreferenceNsql = null;
        if (vo != null) {
            userWidgetPreferenceNsql = new UserWidgetPreferenceNsql();
            UserWidgetPreference data = new UserWidgetPreference();
            FilterPreferences filterPreferences = new FilterPreferences();
            if (vo.getId() != null)
                userWidgetPreferenceNsql.setId(vo.getId());
            data.setUserId(vo.getUserId());
            FilterPreferencesVO filterPrefVO = vo.getFilterPreferences();
            FilterPreferences filterPref = new FilterPreferences();
            if (filterPrefVO != null) {
                BeanUtils.copyProperties(filterPrefVO, filterPref);

                if (filterPrefVO.getCarlafunction() != null) {
                     List<WidgetUserCarlafunction> carlafunction= new ArrayList<>();;
                     carlafunction = filterPrefVO.getCarlafunction().stream().map(n -> this.toWidgetCarlafunction(n)).collect(Collectors.toList());
                     filterPref.setCarlafunction(carlafunction);
                 }
                if (filterPrefVO.getPlatform() != null) {
                    List<WidgetUserPlatform> widgetUserPlatforms = new ArrayList<>();;
                    widgetUserPlatforms = filterPrefVO.getPlatform().stream().map(n -> this.toWidgetPlatform(n)).collect(Collectors.toList());
                    filterPref.setPlatform(widgetUserPlatforms);
                }
                if (filterPrefVO.getAgileReleaseTrain() != null) {
                    List<WidgetUserAgileReleaseTrain> widgetuserAggileReleaseTrain = new ArrayList<>();;
                    widgetuserAggileReleaseTrain = filterPrefVO.getAgileReleaseTrain().stream().map(n -> this.toWidgetAgileReleaseTranin(n)).collect(Collectors.toList());
                    filterPref.setAgileReleaseTrain(widgetuserAggileReleaseTrain);
                }
                if (filterPrefVO.getFrontendTools() != null) {
                    List<WidgetUserFrontEndTools> widgetUserFrontEndTools= new ArrayList<>();;
                    widgetUserFrontEndTools = filterPrefVO.getFrontendTools().stream().map(n -> this.toWidgetFrontEndTools(n)).collect(Collectors.toList());
                    filterPref.setFrontendTools(widgetUserFrontEndTools);
                }
            }
            data.setFilterPreferences(filterPref);
            userWidgetPreferenceNsql.setData(data);
        }
        return userWidgetPreferenceNsql;
    }
}
