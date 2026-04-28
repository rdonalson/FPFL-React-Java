export interface InitialAmountRequest {
  userId: string;
  name: string;
  amount: number;
  fkItemType: number; // always 3 for InitialAmount
  beginDate?: string | null;
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
