package main.java.me.test;

final class LoginPage {
    private LoginPage() {}

    static String html(String error) {
        String err = (error == null) ? "" :
                "<article style='border-color:#e33'><b>" + Auth.escapeHtml(error) + "</b></article>";
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width,initial-scale=1"/>
              <title>Sign in</title>
	      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">
              <style>
                header.topbar{position:sticky;top:0;z-index:10;background:var(--pico-background-color);
                  border-bottom:1px solid var(--pico-muted-border-color)}
                header.topbar nav{display:flex;justify-content:space-between;align-items:center}
                main{max-width:420px;margin-top:10vh}
              </style>
            </head>
            <body>
              <header class="topbar">
                <nav class="container">
                  <strong>Helidon Demo</strong>
                  <span></span>
                </nav>
              </header>
              <main class="container">
                <hgroup><h1>Sign in</h1><p>Access the Helidon UI</p></hgroup>
                %s
                <form method="post" action="/auth/login">
                  <label>Username <input name="username" autocomplete="username" required></label><br>
                  <label>Password <input type="password" name="password" autocomplete="current-password" required></label><br>
                  <button type="submit">Sign in</button>
                </form>
              </main>
            </body>
            </html>
            """.formatted(err);
    }
}
