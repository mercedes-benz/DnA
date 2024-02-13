package com.daimler.data.assembler;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import com.daimler.data.dto.workspace.recipe.RecipeVO.OSNameEnum;
import com.daimler.data.dto.workspace.CodeServerRecipeDetailsVO.RecipeIdEnum;
import com.daimler.data.dto.workspace.UserInfoVO;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.json.CodeServerRecipe;
import com.daimler.data.db.json.RecipeSoftware;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.dto.workspace.recipe.RecipeSoftwareVO;
import com.daimler.data.db.json.CodeServerSoftware;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;

@Slf4j
@Component
public class SoftwareAssembler implements GenericAssembler<SoftwareCollection, CodeServerSoftwareNsql> {

	@Override
	public SoftwareCollection toVo(CodeServerSoftwareNsql entity) {
        SoftwareCollection vo = new SoftwareCollection();
		// TODO Auto-generated method stub
        CodeServerSoftware data = entity.getData();
        System.out.println(data);
        if(Objects.nonNull(data))
        {
            vo.setSoftwareName(entity.getData().getSoftwareName());
        }
        System.out.println(vo);
		return vo;
	}

	@Override
	public CodeServerSoftwareNsql toEntity(SoftwareCollection vo) {
        CodeServerSoftwareNsql entity = new CodeServerSoftwareNsql();
        CodeServerSoftware data = new CodeServerSoftware();
        if(vo!=null)
        {
           data.setSoftwareName(vo.getSoftwareName());
            entity.setData(data);
        }
        return entity;
	}

}
