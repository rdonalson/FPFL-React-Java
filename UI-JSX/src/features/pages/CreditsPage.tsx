import { useEffect, useState } from 'react';

interface Item {
  id: number;
  userId: string;
  name: string;
  amount: number;
  itemType: {
    id: number;
    name: string;
  };
  TimePeriod: {
    id: number;
    name: string;
  };
  beginDate: string | null;
}

const USER_ID = '3fa85f64-5717-4562-b3fc-2c963f66afa6';
const ITEM_TYPE = 1;
const API_BASE = 'http://localhost:9000';

export default function CreditsPage() {
  const [credits, setCredits] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadCredits() {
      try {
        const response = await fetch(`${API_BASE}/items/${USER_ID}/${ITEM_TYPE}`);

        if (!response.ok) {
          throw new Error(`Failed to load credits: ${response.status}`);
        }

        const apiResponse = await response.json();
        const credits: Item[] = apiResponse.data || [];
        setCredits(credits);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    loadCredits();
  }, []);

  if (loading) {
    return <div style={{ padding: '2rem' }}>Loading credits…</div>;
  }

  return (
    <div style={{ padding: '2rem' }}>
      <h1>User Credits</h1>

      {credits.length === 0 && <p>No credits found.</p>}

      <ul>
        {credits.map(c => (
          <li key={c.id} style={{ marginBottom: '1rem' }}>
            <strong>{c.id}</strong> — {c.name}, {c.amount}
          </li>
        ))}
      </ul>
    </div>
  );
}
