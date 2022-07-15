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

package com.daimler.data.dna.trino.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.dto.Parquet;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParquetReaderClient {

	@Value("${minio.baseUri}")
	private String minioBaseUri;
	
	@Value("${minio.accessKey}")
	private String minioAccessKey;
	
	@Value("${minio.secretKey}")
	private String minioSecretKey;
	
	@Value("${trino.hadoop.remoteUser}")
	private String hadoopRemoteUser;
	
	public String getTrinoType(String parquetDataType) {
		if(parquetDataType!=null) {
			String primitiveType = parquetDataType.toUpperCase();
			switch(primitiveType) {
			case "MAP" : return "MAP";
			case "LIST" : return "ARRAY";
			case "STRING" : return "VARCHAR";
			case "MAPKEYVALUE" : return "MAP";
			case "ENUM" : return "VARCHAR";
			case "DECIMAL" : return "DECIMAL";
			case "DATE" : return "DATE";
			case "TIME" : return "TIME";
			case "TIMESTAMP" : return "TIMESTAMP";
			case "INTEGER" : return "INTEGER";
			case "JSON" : return "JSON";
			case "BSON" : return "VARBINARY";
			case "UUID" : return "VARCHAR";
			case "INTERVAL" : return "VARCHAR";
			case "INT64" : return "BIGINT";
			case "INT32" : return "INTEGER";
			case "BOOLEAN" : return "BOOLEAN";
			case "BINARY" : return "VARBINARY";
			case "FLOAT" : return "REAL";
			case "DOUBLE" : return "DOUBLE";
			case "INT96" : return "TIMESTAMP";
			case "FIXED_LEN_BYTE_ARRAY" : return "ARRAY";
			default : return "VARCHAR";
			}
		}
		return "VARCHAR";
	}
	
	public String getCreateTableStatement(Parquet parquetData, String createTablePrefix) {
		String createTable = "";
		if(parquetData!=null) {
			createTable = createTablePrefix;
			List<Type> fields = parquetData.getSchema();
			if(fields!=null && !fields.isEmpty()) {
				for(int i=0; i<fields.size(); i++) {
					PrimitiveType primitiveColumnInfo = fields.get(i).asPrimitiveType();
					String primitiveDataType = primitiveColumnInfo.getLogicalTypeAnnotation()!=null ? 
							primitiveColumnInfo.getLogicalTypeAnnotation().toString() : primitiveColumnInfo.getPrimitiveTypeName().name(); 
					String columnType = this.getTrinoType(primitiveDataType);
					createTable += " " + primitiveColumnInfo.getName() + " " + columnType;
					if(i< (fields.size()-1))
						createTable += ",";
					else
						createTable += ")";
			      }
			}
		}
		log.info("Generated create table statement, returning");
		return createTable;
	}
	
	public Parquet getParquetData(String filePath) throws Exception {
	      List<SimpleGroup> simpleGroups = new ArrayList<>();
	      Path path = new Path(filePath);
	      UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser(hadoopRemoteUser));
	      Configuration conf = new Configuration();
	      conf.set("fs.s3a.access.key", minioAccessKey);
	      conf.set("fs.s3a.secret.key", minioSecretKey);
	      conf.set("fs.s3a.endpoint", minioBaseUri);
	      conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
	      conf.setBoolean("fs.s3a.path.style.access", true);
	      InputFile file = HadoopInputFile.fromPath(path, conf);
	      log.info("Hadoop inputstream has successfully connected to minio and read file at path {}", filePath);
	      ParquetFileReader reader = ParquetFileReader.open(file);
	      MessageType schema = reader.getFooter().getFileMetaData().getSchema();
	      List<ColumnDescriptor> columns = schema.getColumns();
	      List<Type> fields = schema.getFields();
	      reader.close();
	      log.info("returning parquet object with fields and columns info after reading {}",filePath);
	      return new Parquet(simpleGroups, fields, columns);
	  }
	
		
}
