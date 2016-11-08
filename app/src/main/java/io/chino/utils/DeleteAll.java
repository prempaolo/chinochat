package io.chino.utils;

import io.chino.api.collection.Collection;
import io.chino.api.collection.GetCollectionsResponse;
import io.chino.api.common.ChinoApiException;
import io.chino.api.document.Document;
import io.chino.api.document.GetDocumentsResponse;
import io.chino.api.group.GetGroupsResponse;
import io.chino.api.group.Group;
import io.chino.api.repository.GetRepositoriesResponse;
import io.chino.api.repository.Repository;
import io.chino.api.schema.GetSchemasResponse;
import io.chino.api.schema.Schema;
import io.chino.api.user.GetUsersResponse;
import io.chino.api.user.User;
import io.chino.api.userschema.GetUserSchemasResponse;
import io.chino.api.userschema.UserSchema;
import io.chino.android.ChinoAPI;

import java.io.IOException;
import java.util.List;

public class DeleteAll {

    static String REPOSITORY_ID = "";
    static String USER_SCHEMA_ID = "";
    static String USER_ID = "";

    public static void testDelete(ChinoAPI chino) throws IOException, ChinoApiException {

        GetRepositoriesResponse repositoriesResponse = chino.repositories.list(0);
        List<Repository> repositoriesList = repositoriesResponse.getRepositories();
        for(Repository repo : repositoriesList){
            REPOSITORY_ID = repo.getRepositoryId();
            GetSchemasResponse schemasResponse = chino.schemas.list(repo.getRepositoryId() ,0);
            List<Schema> schemaList = schemasResponse.getSchemas();
            for(Schema schema : schemaList){
                GetDocumentsResponse documentsResponse = chino.documents.list(schema.getSchemaId(), 0);
                List<Document> documentList = documentsResponse.getDocuments();
                for(Document document : documentList){
                    System.out.println(chino.documents.delete(document.getDocumentId(), true));
                }
                System.out.println(chino.schemas.delete(schema.getSchemaId(), true));
            }
            System.out.println(chino.repositories.delete(REPOSITORY_ID, true));
        }

        /*GetUserSchemasResponse userSchemasResponse = chino.userSchemas.list(0);
        List<UserSchema> userSchemaList = userSchemasResponse.getUserSchemas();
        for(UserSchema userSchema : userSchemaList){
            USER_SCHEMA_ID = userSchema.getUserSchemaId();
            GetUsersResponse usersResponse = chino.users.list(0, USER_SCHEMA_ID);
            List<User> userList = usersResponse.getUsers();
            for(User user : userList){
                USER_ID = user.getUserId();
                System.out.println(chino.users.delete(USER_ID, true));
            }
            System.out.println(chino.userSchemas.delete(USER_SCHEMA_ID, true));
        }*/

        GetCollectionsResponse collectionsResponse = chino.collections.list(0);
        List<Collection> collectionList = collectionsResponse.getCollections();
        for(Collection collection : collectionList){
            chino.collections.delete(collection.getCollectionId(), true);
        }

        GetGroupsResponse groupsResponse = chino.groups.list(0);
        List<Group> groupList = groupsResponse.getGroups();
        for(Group group : groupList){
            chino.groups.delete(group.getGroupId(), true);
        }
    }
}