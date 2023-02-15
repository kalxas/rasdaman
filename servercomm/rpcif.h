/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _RPCIF_H_H_RPCGEN
#define _RPCIF_H_H_RPCGEN

using u_long = unsigned long int;
using u_int = unsigned int;
using u_short = unsigned short int;

#ifdef __cplusplus
extern "C" {
#endif


typedef struct {
	u_int confarray_len;
	char *confarray_val;
} confarray;

struct RPCMarray {
	char *domain;
	u_long cellTypeLength;
	u_short currentFormat;
	u_short storageFormat;
	confarray data;
	u_short bandLinearization{0};
	u_short cellLinearization{0};
};
typedef struct RPCMarray RPCMarray;

struct RPCClientEntry {
	u_long clientId;
	char *clientIdText;
	char *userName;
	char *baseName;
	u_long creationTime;
	u_long lastActionTime;
	u_long transferColl;
	u_long transferIter;
	u_long assembleMDD;
	u_long transferMDD;
	u_long transTiles;
	u_long tileIter;
	u_long bytesToTransfer;
};
typedef struct RPCClientEntry RPCClientEntry;

struct RPCOIdEntry {
	char *oid;
};
typedef struct RPCOIdEntry RPCOIdEntry;

struct OpenDBParams {
	char *dbName;
	char *userName;
	char *capability;
};
typedef struct OpenDBParams OpenDBParams;

struct OpenDBRes {
	u_short status;
	u_long clientID;
};
typedef struct OpenDBRes OpenDBRes;

struct BeginTAParams {
	u_long clientID;
	u_short readOnly;
	char *capability;
};
typedef struct BeginTAParams BeginTAParams;

struct ExecuteQueryParams {
	u_long clientID;
	char *query;
};
typedef struct ExecuteQueryParams ExecuteQueryParams;

struct ExecuteQueryRes {
	u_short status;
	u_long errorNo;
	u_long lineNo;
	u_long columnNo;
	char *token;
	char *typeName;
	char *typeStructure;
};
typedef struct ExecuteQueryRes ExecuteQueryRes;

struct ExecuteUpdateRes {
	u_short status;
	u_long errorNo;
	u_long lineNo;
	u_long columnNo;
	char *token;
};
typedef struct ExecuteUpdateRes ExecuteUpdateRes;

struct InsertCollParams {
	u_long clientID;
	char *collName;
	char *typeName;
	char *oid;
};
typedef struct InsertCollParams InsertCollParams;

struct NameSpecParams {
	u_long clientID;
	char *name;
};
typedef struct NameSpecParams NameSpecParams;

struct OIdSpecParams {
	u_long clientID;
	char *oid;
};
typedef struct OIdSpecParams OIdSpecParams;

struct RemoveObjFromCollParams {
	u_long clientID;
	char *collName;
	char *oid;
};
typedef struct RemoveObjFromCollParams RemoveObjFromCollParams;

struct GetCollRes {
	u_short status;
	char *typeName;
	char *typeStructure;
	char *oid;
	char *collName;
};
typedef struct GetCollRes GetCollRes;

struct GetCollOIdsRes {
	u_short status;
	char *typeName;
	char *typeStructure;
	char *oid;
	char *collName;
	struct {
		u_int oidTable_len;
		RPCOIdEntry *oidTable_val;
	} oidTable;
};
typedef struct GetCollOIdsRes GetCollOIdsRes;

struct GetMDDRes {
	u_short status;
	char *domain;
	char *typeName;
	char *typeStructure;
	char *oid;
	u_short currentFormat;
};
typedef struct GetMDDRes GetMDDRes;

struct GetTileRes {
	u_short status;
	RPCMarray *marray;
};
typedef struct GetTileRes GetTileRes;

struct OIdRes {
	u_short status;
	char *oid;
};
typedef struct OIdRes OIdRes;

struct ObjectTypeRes {
	u_short status;
	u_short objType;
};
typedef struct ObjectTypeRes ObjectTypeRes;

struct InsertMDDParams {
	u_long clientID;
	char *collName;
	char *typeName;
	char *oid;
	RPCMarray *marray;
};
typedef struct InsertMDDParams InsertMDDParams;

struct InsertTileParams {
	u_long clientID;
	int isPersistent;
	RPCMarray *marray;
};
typedef struct InsertTileParams InsertTileParams;

struct EndInsertMDDParams {
	u_long clientID;
	int isPersistent;
};
typedef struct EndInsertMDDParams EndInsertMDDParams;

struct InsertTransMDDParams {
	u_long clientID;
	char *collName;
	char *domain;
	u_long typeLength;
	char *typeName;
};
typedef struct InsertTransMDDParams InsertTransMDDParams;

struct InsertPersMDDParams {
	u_long clientID;
	char *collName;
	char *domain;
	u_long typeLength;
	char *typeName;
	char *oid;
};
typedef struct InsertPersMDDParams InsertPersMDDParams;

struct NewOIdParams {
	u_long clientID;
	u_short objType;
};
typedef struct NewOIdParams NewOIdParams;

struct ServerStatRes {
	u_short status;
	u_long inactivityTimeout;
	u_long managementInterval;
	u_long transactionActive;
	u_long maxTransferBufferSize;
	u_long nextClientId;
	u_long clientNumber;
	u_long memArena;
	u_long memSmblks;
	u_long memOrdblks;
	u_long memFordblks;
	u_long memUordblks;
	struct {
		u_int clientTable_len;
		RPCClientEntry *clientTable_val;
	} clientTable;
};
typedef struct ServerStatRes ServerStatRes;

struct ServerVersionRes {
	u_short status;
	double serverVersionNo;
	double rpcInterfaceVersionNo;
};
typedef struct ServerVersionRes ServerVersionRes;

struct GetTypeStructureParams {
	u_long clientID;
	char *typeName;
	u_short typeType;
};
typedef struct GetTypeStructureParams GetTypeStructureParams;

struct GetTypeStructureRes {
	u_short status;
	char *typeStructure;
};
typedef struct GetTypeStructureRes GetTypeStructureRes;

struct GetElementRes {
	u_short status;
	confarray data;
};
typedef struct GetElementRes GetElementRes;

struct SetServerTransferParams {
	u_long clientID;
	u_short format;
	char *formatParams;
};
typedef struct SetServerTransferParams SetServerTransferParams;

struct GetExtendedErrorInfo {
	u_short status;
	char *errorText;
};
typedef struct GetExtendedErrorInfo GetExtendedErrorInfo;

#ifdef __cplusplus
}
#endif

#endif /* !_RPCIF_H_H_RPCGEN */
