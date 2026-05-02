export interface InitialAmountRequest {
  userId: string;
  amount: number;
}

export interface InitialAmountResponse {
  id: number;
  userId: string;
  name: string;
  amount: number;
  itemType: {
    id: number;
    name: string;
  };
  beginDate: string | null;
}
