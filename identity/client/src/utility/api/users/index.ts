import { ApiDefinition, apiDelete, apiPost, apiPut } from "../request";
import { SearchResponse } from "src/utility/api";

export const USERS_ENDPOINT = "/users";

export type User = {
  id: string;
  key: number;
  name: string;
  username: string;
  password: string;
  email: string;
  enabled: boolean;
};

export const searchUser: ApiDefinition<SearchResponse<User>> = () =>
  apiPost(`${USERS_ENDPOINT}/search`);

type GetUserParams = {
  username: string;
};


export const getUserDetails: ApiDefinition<SearchResponse<User>, GetUserParams> = ({ username }) =>
  apiPost(`${USERS_ENDPOINT}/search`, {filter: {username}});

type CreateUserParams = Omit<User, "id" | "key" | "enabled">;

export const createUser: ApiDefinition<undefined, CreateUserParams> = (user) =>
  apiPost(USERS_ENDPOINT, { ...user, enabled: true });

type UpdateUserParams = Omit<User, "enabled">;

export const updateUser: ApiDefinition<undefined, UpdateUserParams> = (user) =>
  apiPut(`${USERS_ENDPOINT}/${user.key}`, { ...user, enabled: true });

type DeleteUserParams = {
    id: string;
};

export const deleteUser: ApiDefinition<undefined, DeleteUserParams> = ({
  id,
}) => apiDelete(`${USERS_ENDPOINT}/${id}`);
