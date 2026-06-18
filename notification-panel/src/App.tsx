import { useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api";

type User = {
    id: number;
    email: string;
};

type MailAccount = {
    id: number;
    gmailAddress: string;
    active: boolean;
};

type GoogleAccount = {
    id: number;
    googleEmail: string;
    active: boolean;
};

type Notification = {
    id: number;
    sender: string;
    recipient: string;
    subject: string;
    content: string;
    type: string;
    status: string;
    receivedAt: string;
    mailAccount?: MailAccount;
};

const typeLabels: Record<string, string> = {
    ALL: "Wszystkie",
    EMAIL: "Poczta",
    CALENDAR: "Kalendarz",
    TASK: "Zadania",
};

const typeLabel = (type: string) => typeLabels[type] ?? type;

function App() {
    const [user, setUser] = useState<User | null>(() => {
        const saved = localStorage.getItem("user");
        return saved ? JSON.parse(saved) : null;
    });

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [gmailAddress, setGmailAddress] = useState("");
    const [appPassword, setAppPassword] = useState("");

    const [mailAccounts, setMailAccounts] = useState<MailAccount[]>([]);
    const [googleAccount, setGoogleAccount] = useState<GoogleAccount | null>(null);
    const [notifications, setNotifications] = useState<Notification[]>([]);

    const [typeFilter, setTypeFilter] = useState("ALL");
    const [mailFilter, setMailFilter] = useState<number | null>(null);

    const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
    const [message, setMessage] = useState("");

    useEffect(() => {
        if (user) {
            loadMailAccounts();
            loadGoogleAccount();
            loadNotifications();
        }
    }, [user]);

    async function register() {
        const res = await fetch(`${API_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!res.ok) {
            setMessage("Błąd rejestracji");
            return;
        }

        const data = await res.json();
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
    }

    async function login() {
        const res = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!res.ok) {
            setMessage("Błąd logowania");
            return;
        }

        const data = await res.json();
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
    }

    async function loadMailAccounts() {
        if (!user) return;
        const res = await fetch(`${API_URL}/mail-accounts/user/${user.id}`);
        setMailAccounts(await res.json());
    }

    async function loadGoogleAccount() {
        if (!user) return;
        const res = await fetch(`${API_URL}/google/user/${user.id}`);
        const data = await res.json();
        setGoogleAccount(data || null);
    }

    async function loadNotifications() {
        if (!user) return;
        const res = await fetch(`${API_URL}/notifications/user/${user.id}`);
        setNotifications(await res.json());
    }

    async function connectGoogle() {
        if (!user) return;
        const res = await fetch(`${API_URL}/google/auth-url?userId=${user.id}`);
        const data = await res.json();
        window.location.href = data.url;
    }

    async function syncGoogle() {
        const res = await fetch(`${API_URL}/google/sync`, {
            method: "POST",
        });

        if (!res.ok) {
            setMessage("Błąd synchronizacji Google");
            return;
        }

        setMessage("Google Calendar & Tasks zsynchronizowane");
        await loadNotifications();
    }

    async function deleteGoogleAccount() {
        if (!googleAccount) return;

        await fetch(`${API_URL}/google/${googleAccount.id}`, {
            method: "DELETE",
        });

        setGoogleAccount(null);
        setMessage("Google odłączone");
    }

    async function addMailAccount() {
        if (!user) return;

        const cleanedPassword = appPassword.replace(/\s/g, "");

        const res = await fetch(`${API_URL}/mail-accounts`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: user.id,
                gmailAddress,
                appPassword: cleanedPassword,
            }),
        });

        if (!res.ok) {
            setMessage("Nie udało się dodać Gmaila");
            return;
        }

        setGmailAddress("");
        setAppPassword("");
        setMessage("Gmail dodany");
        await loadMailAccounts();
    }

    async function deleteMailAccount(id: number) {
        await fetch(`${API_URL}/mail-accounts/${id}`, {
            method: "DELETE",
        });

        setMessage("Gmail usunięty");
        setMailFilter(null);
        await loadMailAccounts();
        await loadNotifications();
    }

    async function syncEmails() {
        const res = await fetch(`${API_URL}/mail-accounts/sync`, {
            method: "POST",
        });

        if (!res.ok) {
            setMessage("Błąd synchronizacji Gmaila");
            return;
        }

        setMessage("Gmail zsynchronizowany");
        await loadNotifications();
    }

    function logout() {
        localStorage.removeItem("user");
        setUser(null);
        setNotifications([]);
        setMailAccounts([]);
        setGoogleAccount(null);
        setSelectedNotification(null);
        setMailFilter(null);
        setTypeFilter("ALL");
        setMessage("");
    }

    function formatDate(date: string) {
        if (!date) return "";
        return new Date(date).toLocaleString("pl-PL");
    }

    const filteredNotifications = notifications.filter((notification) => {
        const typeOk = typeFilter === "ALL" || notification.type === typeFilter;
        const mailOk = mailFilter === null || notification.mailAccount?.id === mailFilter;
        return typeOk && mailOk;
    });

    function countType(type: string) {
        if (type === "ALL") return notifications.length;
        return notifications.filter((notification) => notification.type === type).length;
    }

    if (!user) {
        return (
            <div className="auth-page">
                <div className="auth-card">
                    <div className="auth-logo">NotifyDesk</div>
                    <h1>Centrum powiadomień</h1>
                    <p>Poczta, kalendarz i zadania w jednym dashboardzie.</p>

                    <input
                        placeholder="Adres e-mail"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                    />

                    <input
                        placeholder="Hasło"
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                    />

                    <div className="auth-buttons">
                        <button onClick={login}>Zaloguj się</button>
                        <button className="secondary" onClick={register}>
                            Zarejestruj się
                        </button>
                    </div>

                    {message && <span className="message">{message}</span>}
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard">
            <aside className="sidebar">
                <div className="brand">
                    <h2>NotifyDesk</h2>
                    <span>{user.email}</span>
                </div>

                <div className="dual-actions">
                    <button className="sync-button" onClick={syncEmails}>
                        Synchronizuj Gmail
                    </button>

                    <button className="sync-button google" onClick={syncGoogle}>
                        Synchronizuj Google
                    </button>
                </div>

                <section className="sidebar-section">
                    <div className="section-title">Typ powiadomień</div>

                    {["ALL", "EMAIL", "CALENDAR", "TASK"].map((item) => (
                        <button
                            key={item}
                            className={typeFilter === item ? "nav-item active" : "nav-item"}
                            onClick={() => setTypeFilter(item)}
                        >
                            <strong>{typeLabel(item)}</strong>
                            <span>{countType(item)} powiadomień</span>
                        </button>
                    ))}
                </section>

                <section className="sidebar-section">
                    <div className="section-title">Konta Gmail</div>

                    <button
                        className={mailFilter === null ? "nav-item active" : "nav-item"}
                        onClick={() => setMailFilter(null)}
                    >
                        <strong>Wszystkie skrzynki</strong>
                        <span>{countType("EMAIL")} emaili</span>
                    </button>

                    {mailAccounts.map((account) => (
                        <div className="mail-account-row" key={account.id}>
                            <button
                                className={mailFilter === account.id ? "nav-item active" : "nav-item"}
                                onClick={() => {
                                    setTypeFilter("EMAIL");
                                    setMailFilter(account.id);
                                }}
                            >
                                <strong>{account.gmailAddress}</strong>
                                <span>{account.active ? "aktywne" : "wył."}</span>
                            </button>

                            <button
                                className="delete-mail"
                                onClick={() => deleteMailAccount(account.id)}
                            >
                                Usuń
                            </button>
                        </div>
                    ))}
                </section>

                <section className="integration-box">
                    <h3>Google Calendar & Tasks</h3>

                    {googleAccount ? (
                        <>
                            <div className="connected-account">
                                <strong>{googleAccount.googleEmail}</strong>
                                <span>Połączone</span>
                            </div>

                            <button className="danger-button" onClick={deleteGoogleAccount}>
                                Odłącz Google
                            </button>
                        </>
                    ) : (
                        <button onClick={connectGoogle}>Połącz Google</button>
                    )}

                    <small>Calendar i Tasks są pobierane z głównego konta Google.</small>
                </section>

                <section className="integration-box">
                    <h3>Dodaj Gmail IMAP</h3>

                    <input
                        placeholder="Adres Gmail"
                        value={gmailAddress}
                        onChange={(event) => setGmailAddress(event.target.value)}
                    />

                    <input
                        placeholder="Hasło aplikacji"
                        type="password"
                        value={appPassword}
                        onChange={(event) => setAppPassword(event.target.value)}
                    />

                    <button onClick={addMailAccount}>Dodaj Gmail</button>

                    <a
                        href="https://myaccount.google.com/apppasswords"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Wygeneruj hasło aplikacji
                    </a>
                </section>

                <button className="logout" onClick={logout}>
                    Wyloguj
                </button>
            </aside>

            <main className="main">
                <header className="topbar">
                    <div>
                        <h1>Powiadomienia</h1>
                        <p>Poczta, kalendarz i zadania w jednym miejscu.</p>
                    </div>

                    {message && <div className="toast">{message}</div>}
                </header>

                <section className="stats-row">
                    <div className="stat-card">
                        <span>Wszystkie</span>
                        <strong>{notifications.length}</strong>
                    </div>

                    <div className="stat-card">
                        <span>Poczta</span>
                        <strong>{countType("EMAIL")}</strong>
                    </div>

                    <div className="stat-card">
                        <span>Kalendarz</span>
                        <strong>{countType("CALENDAR")}</strong>
                    </div>

                    <div className="stat-card">
                        <span>Zadania</span>
                        <strong>{countType("TASK")}</strong>
                    </div>
                </section>

                <section className="content">
                    <div className="notifications-list">
                        {filteredNotifications.length === 0 && (
                            <div className="empty-list">
                                Brak powiadomień dla wybranego filtra.
                            </div>
                        )}

                        {filteredNotifications.map((notification) => (
                            <article
                                key={notification.id}
                                className={
                                    selectedNotification?.id === notification.id
                                        ? "notification-card selected"
                                        : "notification-card"
                                }
                                onClick={() => setSelectedNotification(notification)}
                            >
                                <div className="card-top">
                                    <span className={`badge ${notification.type.toLowerCase()}`}>
                                        {typeLabel(notification.type)}
                                    </span>

                                    <span className="date">{formatDate(notification.receivedAt)}</span>
                                </div>

                                <h3>{notification.subject || "(Brak tematu)"}</h3>
                                <p>{notification.sender}</p>

                                <div className="recipient">Do: {notification.recipient}</div>
                            </article>
                        ))}
                    </div>

                    <div className="preview">
                        {selectedNotification ? (
                            <>
                                <span className={`badge big ${selectedNotification.type.toLowerCase()}`}>
                                    {typeLabel(selectedNotification.type)}
                                </span>

                                <h2>{selectedNotification.subject || "(Brak tematu)"}</h2>

                                <div className="meta">
                                    <p><b>Od:</b> {selectedNotification.sender}</p>
                                    <p><b>Do:</b> {selectedNotification.recipient}</p>
                                    <p><b>Data:</b> {formatDate(selectedNotification.receivedAt)}</p>
                                </div>

                                <div className="mail-content">
                                    {selectedNotification.content || "Brak treści"}
                                </div>
                            </>
                        ) : (
                            <div className="empty-preview">Wybierz powiadomienie</div>
                        )}
                    </div>
                </section>
            </main>
        </div>
    );
}

export default App;
