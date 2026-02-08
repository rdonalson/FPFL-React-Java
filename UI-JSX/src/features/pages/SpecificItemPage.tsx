import { InlineError } from '@/components/InLineError';
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

const ID = 100;
//const ID = 14; //http://localhost:9000/items/14
const API_BASE = 'http://localhost:9000'; //http://localhost:9000/items/10

export default function SpecificItemPage() {
  const [credit, setCredit] = useState<Item | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadCredits() {
      try {
        const response = await fetch(`${API_BASE}/items/${ID}`);


        // if (!response.ok) {
        //   return <InlineError message={`Failed to load item: ${response.status}`} />;
        //   //throw new Error(`Failed to load credits: ${response.status}`);
        // }

        const apiResponse = await response.json();
        const credit: Item = apiResponse.data || null;
        setCredit(credit);
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
      <h1>User Credit</h1>

      {credit === null && 
      <p>No credit found.
        { <InlineError message={'No credit found.'} /> }
      </p>}

      {credit && (
        <ul>
          <li key={credit.id} style={{ marginBottom: '1rem' }}>
            <strong>{credit.id}</strong> — {credit.name}, {credit.amount}
          </li>
        </ul>
        
      )}
    </div>
  );
}
