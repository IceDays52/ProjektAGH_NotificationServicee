import { useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api";

type User = {
    id: number;
    email: string;
    role: string;
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

type AdminStats = {
    usersCount: number;
    adminsCount: number;
    mailAccountsCount: number;
    googleAccountsCount: number;
    notificationsCount: number;
};

type AdminUser = {
    id: number;
    email: string;
    role: string;
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

    const [adminStats, setAdminStats] = useState<AdminStats | null>(null);
    const [adminUsers, setAdminUsers] = useState<AdminUser[]>([]);

    const [typeFilter, setTypeFilter] = useState("ALL");
    const [mailFilter, setMailFilter] = useState<number | null>(null);

    const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
    const [message, setMessage] = useState("");

    const isAdmin = user?.role === "ADMIN";

    useEffect(() => {
        if (!user) return;

        if (user.role === "ADMIN") {
            loadAdminData();
        } else {
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

    async function loadAdminData() {
        const statsRes = await fetch(`${API_URL}/admin/stats`);
        const usersRes = await fetch(`${API_URL}/admin/users`);

        if (!statsRes.ok || !usersRes.ok) {
            setMessage("Nie udało się pobrać danych administratora");
            return;
        }

        setAdminStats(await statsRes.json());
        setAdminUsers(await usersRes.json());
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
        setAdminStats(null);
        setAdminUsers([]);
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

    if (isAdmin) {
        return (
            <div className="dashboard">
                <aside className="sidebar">
                    <div className="brand">
                        <h2>NotifyDesk</h2>
                        <span>{user.email}</span>

                        <div
                            style={{
                                marginTop: "10px",
                                padding: "8px 12px",
                                borderRadius: "8px",
                                background: "#ff9800",
                                color: "white",
                                fontWeight: "bold",
                                textAlign: "center",
                            }}
                        >
                            ADMIN DESK
                        </div>
                    </div>

                    <section className="sidebar-section">
                        <div className="section-title">Panel administratora</div>

                        <button className="nav-item active" onClick={loadAdminData}>
                            <strong>Dashboard</strong>
                            <span>Statystyki systemu</span>
                        </button>
                    </section>

                    <button className="logout" onClick={logout}>
                        Wyloguj
                    </button>
                </aside>

                <main className="main">
                    <header className="topbar">
                        <div>
                            <h1>Admin Desk</h1>
                            <p>Statystyki użytkowników, kont i powiadomień w systemie.</p>
                        </div>

                        {message && <div className="toast">{message}</div>}
                    </header>

                    <section className="stats-row">
                        <div className="stat-card">
                            <span>Użytkownicy</span>
                            <strong>{adminStats?.usersCount ?? 0}</strong>
                        </div>

                        <div className="stat-card">
                            <span>Admini</span>
                            <strong>{adminStats?.adminsCount ?? 0}</strong>
                        </div>

                        <div className="stat-card">
                            <span>Konta Gmail</span>
                            <strong>{adminStats?.mailAccountsCount ?? 0}</strong>
                        </div>

                        <div className="stat-card">
                            <span>Konta Google</span>
                            <strong>{adminStats?.googleAccountsCount ?? 0}</strong>
                        </div>

                        <div className="stat-card">
                            <span>Powiadomienia</span>
                            <strong>{adminStats?.notificationsCount ?? 0}</strong>
                        </div>
                    </section>

                    <section
                        style={{
                            marginTop: "24px",
                            padding: "24px",
                            borderRadius: "24px",
                            background: "rgba(15, 15, 25, 0.95)",
                            border: "1px solid rgba(255,255,255,0.08)",
                        }}
                    >
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                marginBottom: "18px",
                            }}
                        >
                            <div>
                                <h2>Użytkownicy systemu</h2>
                                <p style={{ color: "#9ca3af", marginTop: "6px" }}>
                                    Lista kont z rolami USER / ADMIN.
                                </p>
                            </div>

                            <button
                                className="admin-button"
                                onClick={loadAdminData}
                            >
                                Odśwież dane
                            </button>
                        </div>

                        <table
                            className="admin-table"
                        >
                            <thead>
                            <tr style={{ background: "#1d1d2e" }}>
                                <th style={{ padding: "14px", textAlign: "left" }}>ID</th>
                                <th style={{ padding: "14px", textAlign: "left" }}>Email</th>
                                <th style={{ padding: "14px", textAlign: "left" }}>Rola</th>
                            </tr>
                            </thead>

                            <tbody>
                            {adminUsers.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan={3}
                                        style={{
                                            padding: "20px",
                                            textAlign: "center",
                                            color: "#9ca3af",
                                        }}
                                    >
                                        Brak użytkowników do wyświetlenia.
                                    </td>
                                </tr>
                            ) : (
                                adminUsers.map((adminUser) => (
                                    <tr
                                        key={adminUser.id}
                                        style={{
                                            borderTop: "1px solid rgba(255,255,255,0.08)",
                                        }}
                                    >
                                        <td style={{ padding: "14px" }}>{adminUser.id}</td>
                                        <td style={{ padding: "14px" }}>{adminUser.email}</td>
                                        <td style={{ padding: "14px" }}>
                                                <span
                                                    style={{
                                                        padding: "6px 12px",
                                                        borderRadius: "999px",
                                                        background:
                                                            adminUser.role === "ADMIN"
                                                                ? "#ff9800"
                                                                : "#2d6cdf",
                                                        color: "white",
                                                        fontWeight: "bold",
                                                    }}
                                                >
                                                    {adminUser.role}
                                                </span>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </section>

                    <section
                        style={{
                            marginTop: "24px",
                            padding: "24px",
                            borderRadius: "24px",
                            background: "rgba(15, 15, 25, 0.95)",
                            border: "1px solid rgba(255,255,255,0.08)",
                        }}
                    >
                        <h2>Podsumowanie systemu</h2>

                        <table
                            style={{
                                width: "100%",
                                marginTop: "18px",
                                borderCollapse: "collapse",
                                background: "#11111c",
                                borderRadius: "16px",
                                overflow: "hidden",
                            }}
                        >
                            <thead>
                            <tr style={{ background: "#1d1d2e" }}>
                                <th style={{ padding: "14px", textAlign: "left" }}>Metryka</th>
                                <th style={{ padding: "14px", textAlign: "left" }}>Wartość</th>
                            </tr>
                            </thead>

                            <tbody>
                            <tr style={{ borderTop: "1px solid rgba(255,255,255,0.08)" }}>
                                <td style={{ padding: "14px" }}>Wszyscy użytkownicy</td>
                                <td style={{ padding: "14px" }}>{adminStats?.usersCount ?? 0}</td>
                            </tr>

                            <tr style={{ borderTop: "1px solid rgba(255,255,255,0.08)" }}>
                                <td style={{ padding: "14px" }}>Administratorzy</td>
                                <td style={{ padding: "14px" }}>{adminStats?.adminsCount ?? 0}</td>
                            </tr>

                            <tr style={{ borderTop: "1px solid rgba(255,255,255,0.08)" }}>
                                <td style={{ padding: "14px" }}>Podłączone konta Gmail</td>
                                <td style={{ padding: "14px" }}>
                                    {adminStats?.mailAccountsCount ?? 0}
                                </td>
                            </tr>

                            <tr style={{ borderTop: "1px solid rgba(255,255,255,0.08)" }}>
                                <td style={{ padding: "14px" }}>Podłączone konta Google</td>
                                <td style={{ padding: "14px" }}>
                                    {adminStats?.googleAccountsCount ?? 0}
                                </td>
                            </tr>

                            <tr style={{ borderTop: "1px solid rgba(255,255,255,0.08)" }}>
                                <td style={{ padding: "14px" }}>Wszystkie powiadomienia</td>
                                <td style={{ padding: "14px" }}>
                                    {adminStats?.notificationsCount ?? 0}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </section>
                </main>
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
                                className={
                                    mailFilter === account.id ? "nav-item active" : "nav-item"
                                }
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

                                    <span className="date">
                                        {formatDate(notification.receivedAt)}
                                    </span>
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
                                <span
                                    className={`badge big ${selectedNotification.type.toLowerCase()}`}
                                >
                                    {typeLabel(selectedNotification.type)}
                                </span>

                                <h2>{selectedNotification.subject || "(Brak tematu)"}</h2>

                                <div className="meta">
                                    <p>
                                        <b>Od:</b> {selectedNotification.sender}
                                    </p>
                                    <p>
                                        <b>Do:</b> {selectedNotification.recipient}
                                    </p>
                                    <p>
                                        <b>Data:</b>{" "}
                                        {formatDate(selectedNotification.receivedAt)}
                                    </p>
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