DROP TABLE IF EXISTS Package CASCADE;

CREATE TABLE Package
(
  <#if dbType=="postgres">
  id serial,
  <#else>
  id int auto_increment,
  </#if>
  defaultOrganizationID text,
  title text,
  base text,
  resourcesBase text,
  summary text,
  assetRefID bigint,
  courseID integer,
  CONSTRAINT Package_pk PRIMARY KEY (id)
<#if dbType=="postgres">
) WITH (
  OIDS=FALSE
</#if>
);