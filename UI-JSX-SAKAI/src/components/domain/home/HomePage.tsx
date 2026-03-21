export default function HomePage() {
    
    console.log("Running in:", process.env.NODE_ENV);

    return (
        <div className="card">
            <h2>Welcome to UI-JSX-SAKAI</h2>
            <p>This is your Next.js + Sakai + Sass starter.</p>
        </div>
    );
}
