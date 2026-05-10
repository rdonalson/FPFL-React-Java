export interface InitialAmountRequest {
  userID: string;
  amount: number;
}

export interface InitialAmountResponse {
  id: number;
  userID: string;
  name: string;
  amount: number;
  itemType: {
    id: number;
    name: string;
  };
  beginDate: string | null;
}
