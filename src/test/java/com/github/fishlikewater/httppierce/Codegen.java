package com.github.fishlikewater.httppierce;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.EntityConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月07日 14:58
 **/
public class Codegen {

    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:sqlite:db.sqlite");


        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle1();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig, IDialect.SQLITE);
        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle1() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();
        JdbcTypeMapping.registerMapping(Timestamp.class, LocalDateTime.class);
        //设置根包
        globalConfig.getPackageConfig()
                .setSourceDir("E:\\IdeaProjects2\\http-pierce\\src\\main\\java")
                .setBasePackage("com.github.fishlikewater.httppierce");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setOverwriteEnable(true)
                .setWithLombok(true)
                .setWithActiveRecord(true)
                .setWithSwagger(false);
        //globalConfig.enableTableDef().setOverwriteEnable(true);
        //设置生成 mapper
        globalConfig.enableMapper().setOverwriteEnable(true);
        //设置生成 service
        globalConfig.enableService().setOverwriteEnable(true);
        //设置生成 serviceImpl
        globalConfig.enableServiceImpl().setOverwriteEnable(true);


        //设置表前缀和只生成哪些表
        //globalConfig.setGenerateSchema("schema");
        //globalConfig.setTablePrefix("tb_");
        globalConfig.setGenerateTable("service_mapping");

        return globalConfig;
    }


}
