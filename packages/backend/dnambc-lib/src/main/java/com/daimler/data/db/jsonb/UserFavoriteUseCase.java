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

package com.daimler.data.db.jsonb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteUseCase implements Serializable {

	private static final long serialVersionUID = -5471958263658241317L;

	String id;
	String usecaseId;
	String userinfoId;

//    public UserFavoriteUseCase() {
//        super();
//    }
//
//
//    public UserFavoriteUseCase(String id, String usecaseId, String userinfoId) {
//        super();
//        this.id = id;
//        this.usecaseId = usecaseId;
//        this.userinfoId = userinfoId;
//    }
//
//
//    public String getId() {
//        return id;
//    }
//
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//
//    public String getUsecaseId() {
//        return usecaseId;
//    }
//
//
//    public void setUsecaseId(String usecaseId) {
//        this.usecaseId = usecaseId;
//    }
//
//
//    public String getUserinfoId() {
//        return userinfoId;
//    }
//
//
//    public void setUserinfoId(String userinfoId) {
//        this.userinfoId = userinfoId;
//    }

}
