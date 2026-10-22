-- DROP SCHEMA dbo;

CREATE SCHEMA dbo;
-- PWInventory.dbo.Entity definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Entity;

CREATE TABLE PWInventory.dbo.Entity (
	Entity_Key int IDENTITY(1,1) NOT NULL,
	Entity varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Address1 varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Address2 varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	City varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	State varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Country varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ZipCode varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT IX_Entity UNIQUE (Entity),
	CONSTRAINT PK_Entity PRIMARY KEY (Entity_Key)
);


-- PWInventory.dbo.Item_Status definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item_Status;

CREATE TABLE PWInventory.dbo.Item_Status (
	Item_Status_Key int IDENTITY(1,1) NOT NULL,
	Status_Name varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Status_Desc varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Default_Status bit DEFAULT 0 NOT NULL,
	Status_Index int NOT NULL,
	CONSTRAINT PK_Item_Status PRIMARY KEY (Item_Status_Key)
);


-- PWInventory.dbo.Item_Type definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item_Type;

CREATE TABLE PWInventory.dbo.Item_Type (
	Item_Type_Key int IDENTITY(1,1) NOT NULL,
	Item_Type_Name varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Item_Type_Desc varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Item_Type_Index int NOT NULL,
	CONSTRAINT PK_Item_Type PRIMARY KEY (Item_Type_Key)
);


-- PWInventory.dbo.Operation definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Operation;

CREATE TABLE PWInventory.dbo.Operation (
	Operation_Key int IDENTITY(1,1) NOT NULL,
	Operation nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_Operation PRIMARY KEY (Operation_Key)
);


-- PWInventory.dbo.Route definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Route;

CREATE TABLE PWInventory.dbo.Route (
	Route_Key int IDENTITY(1,1) NOT NULL,
	Route nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Version nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_Route PRIMARY KEY (Route_Key)
);


-- PWInventory.dbo.RouteOp definition

-- Drop table

-- DROP TABLE PWInventory.dbo.RouteOp;

CREATE TABLE PWInventory.dbo.RouteOp (
	RouteOp_Key int IDENTITY(1,1) NOT NULL,
	RouteOp nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Route nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Operation nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Station nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	CycleTime nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	XToScrap nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	XToNewRoute nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	XFromOtherRoute nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	XFromOutOfRoute nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	XOutOfRoute nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_RouteOp PRIMARY KEY (RouteOp_Key)
);


-- PWInventory.dbo.SourceOp definition

-- Drop table

-- DROP TABLE PWInventory.dbo.SourceOp;

CREATE TABLE PWInventory.dbo.SourceOp (
	SourceOp_Key int IDENTITY(1,1) NOT NULL,
	AssociatedRouteOp nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	SourceOp nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	AssociatedRoute nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_SourceOp PRIMARY KEY (SourceOp_Key)
);


-- PWInventory.dbo.Location definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Location;

CREATE TABLE PWInventory.dbo.Location (
	Location_Key int IDENTITY(1,1) NOT NULL,
	Entity_Key int NOT NULL,
	Location varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT IX_Location UNIQUE (Entity_Key,Location),
	CONSTRAINT PK_Location PRIMARY KEY (Location_Key),
	CONSTRAINT FK_Location_Entity FOREIGN KEY (Entity_Key) REFERENCES PWInventory.dbo.Entity(Entity_Key)
);


-- PWInventory.dbo.Part definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Part;

CREATE TABLE PWInventory.dbo.Part (
	Part_Key int IDENTITY(1,1) NOT NULL,
	Entity_Key int NOT NULL,
	Part_Number varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Part_Desc varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Is_Prototype bit DEFAULT 0 NOT NULL,
	Is_Production bit DEFAULT 0 NOT NULL,
	Is_Active bit DEFAULT 1 NOT NULL,
	Company varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Is_Serialized bit DEFAULT 0 NOT NULL,
	CONSTRAINT IX_Part UNIQUE (Entity_Key,Part_Number),
	CONSTRAINT PK_Part PRIMARY KEY (Part_Key),
	CONSTRAINT FK_Part_Entity FOREIGN KEY (Entity_Key) REFERENCES PWInventory.dbo.Entity(Entity_Key)
);


-- PWInventory.dbo.Part_Attribute definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Part_Attribute;

CREATE TABLE PWInventory.dbo.Part_Attribute (
	PartAttribute_Key int IDENTITY(1,1) NOT NULL,
	AttributeName nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	AttributeDescription nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Part_key int NOT NULL,
	DataType nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT 'String' NOT NULL,
	CONSTRAINT PK_Part_Attribute PRIMARY KEY (PartAttribute_Key),
	CONSTRAINT FK_Part_Attribute_Part FOREIGN KEY (Part_key) REFERENCES PWInventory.dbo.Part(Part_Key)
);
ALTER TABLE PWInventory.dbo.Part_Attribute WITH NOCHECK ADD CONSTRAINT CK_Part_Attribute_DataType CHECK (([DataType]='Boolean' OR [DataType]='DateTime' OR [DataType]='Real' OR [DataType]='Integer' OR [DataType]='String'));


-- PWInventory.dbo.Part_Subpart definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Part_Subpart;

CREATE TABLE PWInventory.dbo.Part_Subpart (
	Subpart_Key int IDENTITY(1,1) NOT NULL,
	Parent_Part_Key int NOT NULL,
	Child_Part_Key int NOT NULL,
	Quantity int NOT NULL,
	CONSTRAINT PK_Part_Subpart PRIMARY KEY (Subpart_Key),
	CONSTRAINT FK_Part_Subpart_Child FOREIGN KEY (Child_Part_Key) REFERENCES PWInventory.dbo.Part(Part_Key),
	CONSTRAINT FK_Part_Subpart_Parent FOREIGN KEY (Parent_Part_Key) REFERENCES PWInventory.dbo.Part(Part_Key)
);


-- PWInventory.dbo.Plant definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Plant;

CREATE TABLE PWInventory.dbo.Plant (
	Plant_Key int IDENTITY(1,1) NOT NULL,
	Location_Key int NOT NULL,
	Plant varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_Plant PRIMARY KEY (Plant_Key),
	CONSTRAINT FK_Plant_Location FOREIGN KEY (Location_Key) REFERENCES PWInventory.dbo.Location(Location_Key)
);


-- PWInventory.dbo.Item definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item;

CREATE TABLE PWInventory.dbo.Item (
	Item_Key int IDENTITY(1,1) NOT NULL,
	Entity_Key int NOT NULL,
	Serial_Nbr varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Part_Key int NOT NULL,
	Type_Key int NOT NULL,
	Current_RouteOp_Key int NULL,
	Status_Key int NOT NULL,
	Birth_DateTime datetime DEFAULT getdate() NOT NULL,
	Birth_RouteOp_Key int NULL,
	LotQuantity smallint NULL,
	CONSTRAINT IX_Item_Entity_Serial UNIQUE (Entity_Key,Serial_Nbr),
	CONSTRAINT PK_Item PRIMARY KEY (Item_Key),
	CONSTRAINT FK_Item_Entity FOREIGN KEY (Entity_Key) REFERENCES PWInventory.dbo.Entity(Entity_Key),
	CONSTRAINT FK_Item_Part FOREIGN KEY (Part_Key) REFERENCES PWInventory.dbo.Part(Part_Key),
	CONSTRAINT FK_Item_Status FOREIGN KEY (Status_Key) REFERENCES PWInventory.dbo.Item_Status(Item_Status_Key),
	CONSTRAINT FK_Item_Type FOREIGN KEY (Type_Key) REFERENCES PWInventory.dbo.Item_Type(Item_Type_Key)
);


-- PWInventory.dbo.Item_Attribute definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item_Attribute;

CREATE TABLE PWInventory.dbo.Item_Attribute (
	ItemAttibute_Key int IDENTITY(1,1) NOT NULL,
	Attribute_Key int NOT NULL,
	Attribute_Value nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Item_Key int NOT NULL,
	Value_Integer bigint NULL,
	Value_Real float NULL,
	Value_DateTime datetime2(3) NULL,
	Value_Boolean bit NULL,
	CONSTRAINT PK_Item_Attribute PRIMARY KEY (ItemAttibute_Key),
	CONSTRAINT FK_Item_Attribute_Item FOREIGN KEY (Item_Key) REFERENCES PWInventory.dbo.Item(Item_Key),
	CONSTRAINT FK_Item_Attribute_Part_Attribute FOREIGN KEY (Attribute_Key) REFERENCES PWInventory.dbo.Part_Attribute(PartAttribute_Key)
);
 CREATE NONCLUSTERED INDEX IX_Item_Attribute_DateTime ON PWInventory.dbo.Item_Attribute (  Attribute_Key ASC  , Value_DateTime ASC  )  
	 WHERE  ([Value_DateTime] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_Item_Attribute_Integer ON PWInventory.dbo.Item_Attribute (  Attribute_Key ASC  , Value_Integer ASC  )  
	 WHERE  ([Value_Integer] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_Item_Attribute_Real ON PWInventory.dbo.Item_Attribute (  Attribute_Key ASC  , Value_Real ASC  )  
	 WHERE  ([Value_Real] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE UNIQUE NONCLUSTERED INDEX UX_Item_Attribute_Item_Attribute ON PWInventory.dbo.Item_Attribute (  Item_Key ASC  , Attribute_Key ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
ALTER TABLE PWInventory.dbo.Item_Attribute WITH NOCHECK ADD CONSTRAINT CK_Item_Attribute_OneTypedValue CHECK (((((case when [Value_Integer] IS NULL then (0) else (1) end+case when [Value_Real] IS NULL then (0) else (1) end)+case when [Value_DateTime] IS NULL then (0) else (1) end)+case when [Value_Boolean] IS NULL then (0) else (1) end)<=(1)));


-- PWInventory.dbo.Item_RouteOp definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item_RouteOp;

CREATE TABLE PWInventory.dbo.Item_RouteOp (
	Item_RouteOp_Key int IDENTITY(1,1) NOT NULL,
	RouteOp_Key int NOT NULL,
	Item_key int NOT NULL,
	Time_In varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Time_Out varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Time_Out_Type varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_Item_RouteOp PRIMARY KEY (Item_RouteOp_Key),
	CONSTRAINT FK_Item_RouteOp_Item FOREIGN KEY (Item_key) REFERENCES PWInventory.dbo.Item(Item_Key),
	CONSTRAINT FK_Item_RouteOp_RouteOp FOREIGN KEY (RouteOp_Key) REFERENCES PWInventory.dbo.RouteOp(RouteOp_Key)
);


-- PWInventory.dbo.Line definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Line;

CREATE TABLE PWInventory.dbo.Line (
	Line_Key int IDENTITY(1,1) NOT NULL,
	Plant_Key int NOT NULL,
	Line varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK_Line PRIMARY KEY (Line_Key),
	CONSTRAINT FK_Line_Plant FOREIGN KEY (Plant_Key) REFERENCES PWInventory.dbo.Plant(Plant_Key)
);


-- PWInventory.dbo.Station definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Station;

CREATE TABLE PWInventory.dbo.Station (
	Station_Key int IDENTITY(1,1) NOT NULL,
	Entity_Key int NOT NULL,
	Line_Key int NOT NULL,
	Station varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Description varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Queue_Max int NULL,
	Login_Required bit NULL,
	X_Position int NULL,
	Y_Position int NULL,
	CONSTRAINT IX_Station UNIQUE (Entity_Key,Station),
	CONSTRAINT PK_Station PRIMARY KEY (Station_Key),
	CONSTRAINT FK_Station_Entity FOREIGN KEY (Entity_Key) REFERENCES PWInventory.dbo.Entity(Entity_Key),
	CONSTRAINT FK_Station_Line FOREIGN KEY (Line_Key) REFERENCES PWInventory.dbo.Line(Line_Key)
);


-- PWInventory.dbo.Item_Gene definition

-- Drop table

-- DROP TABLE PWInventory.dbo.Item_Gene;

CREATE TABLE PWInventory.dbo.Item_Gene (
	Item_Gene_Key int IDENTITY(1,1) NOT NULL,
	Parent_Serial_Number varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	Parent_Part_Number varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	Parent_Item_Key int DEFAULT 0 NULL,
	Parent_Part_Key int DEFAULT 0 NULL,
	Child_Serial_Number varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '0' NOT NULL,
	Child_Part_Number varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '0' NOT NULL,
	Child_Part_Key int DEFAULT 0 NOT NULL,
	Child_Item_Key int DEFAULT 0 NOT NULL,
	Station_Key int DEFAULT 0 NULL,
	Station varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[TimeStamp] AS (getdate()) NOT NULL,
	CONSTRAINT PK_Item_Gene PRIMARY KEY (Item_Gene_Key),
	CONSTRAINT FK_Item_Gene_Parent_Item FOREIGN KEY (Parent_Item_Key) REFERENCES PWInventory.dbo.Item(Item_Key),
	CONSTRAINT FK_Item_Gene_Station FOREIGN KEY (Station_Key) REFERENCES PWInventory.dbo.Station(Station_Key)
);