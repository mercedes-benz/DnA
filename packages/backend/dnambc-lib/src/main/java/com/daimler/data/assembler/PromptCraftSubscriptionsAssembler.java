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


import com.daimler.data.db.entities.PromptCraftSubscriptionsNsql;
import com.daimler.data.db.jsonb.PromptCraftSubscriptions;
import com.daimler.data.db.jsonb.MemberInfo;
import com.daimler.data.dto.promptCraftSubscriptions.PromptCraftSubscriptionsVO;
import com.daimler.data.dto.promptCraftSubscriptions.MemberInfoVO;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class PromptCraftSubscriptionsAssembler implements GenericAssembler<PromptCraftSubscriptionsVO, PromptCraftSubscriptionsNsql> {

@Override
public PromptCraftSubscriptionsVO toVo(PromptCraftSubscriptionsNsql entity) {

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
    PromptCraftSubscriptionsVO vo = new PromptCraftSubscriptionsVO();
    try{
        if (Objects.nonNull(entity)) {
            PromptCraftSubscriptions data = entity.getData();
            vo.setId(entity.getId());
            if(data!=null){
                BeanUtils.copyProperties(data, vo);
                MemberInfoVO memberInfoVO = this.toMemberInfoVO(data.getProjectOwner());
                vo.setProjectOwner(memberInfoVO);
                List<MemberInfo>  memberList =  data.getProjectMembers();
                if(memberList != null){
                    List<MemberInfoVO> memberListVO = memberList.stream().map(n -> toMemberInfoVO(n))
                    .collect(Collectors.toList());
                    vo.setProjectMembers(memberListVO);
                }
                if(data.getCreatedOn() != null){
                    vo.setCreatedOn(isoFormat.parse(isoFormat.format(data.getCreatedOn())));
                }
            }
        }
    }catch(ParseException e){
        log.info("Error in parsing date");
    }
    return vo;
}


@Override
public PromptCraftSubscriptionsNsql toEntity(PromptCraftSubscriptionsVO vo) {
    PromptCraftSubscriptionsNsql entity = null;
    if (vo != null) {
        entity = new PromptCraftSubscriptionsNsql();
        entity.setId(vo.getId());
        PromptCraftSubscriptions data = new PromptCraftSubscriptions();
        BeanUtils.copyProperties(vo, data);
        MemberInfo memberInfo = this. toMemberInfo(vo.getProjectOwner());
        data.setProjectOwner(memberInfo);
        List<MemberInfoVO> merberListVO = vo.getProjectMembers();
        if(merberListVO != null){
            List<MemberInfo> memberList = merberListVO.stream().map(n -> toMemberInfo(n))
            .collect(Collectors.toList());
            data.setProjectMembers(memberList);
        }
        entity.setData(data);
    }
    return entity;
}

public MemberInfoVO toMemberInfoVO(MemberInfo memberInfo){

    MemberInfoVO  memberInfoVO = new MemberInfoVO();
    if(memberInfo != null){
        BeanUtils.copyProperties(memberInfo,memberInfoVO);
    }
    return memberInfoVO;
}

public MemberInfo toMemberInfo(MemberInfoVO vo){

    MemberInfo  memberInfo = new MemberInfo();
    if(vo != null){
        BeanUtils.copyProperties(vo, memberInfo);
    }
    return memberInfo;
}

}
