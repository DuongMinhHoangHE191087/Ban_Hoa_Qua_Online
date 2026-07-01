/**
 * MailTM API client to create temporary emails and retrieve messages/verification codes
 */
export class MailTMClient {
  private token: string = '';
  public email: string = '';
  private password = 'Password@2026_Secure';

  /**
   * Initialize a new temp mail account
   */
  async initialize(): Promise<string> {
    // 1. Get available domains
    const domainsResponse = await fetch('https://api.mail.tm/domains');
    if (!domainsResponse.ok) {
      throw new Error(`Failed to fetch domains from MailTM: ${domainsResponse.statusText}`);
    }
    const domainsData = (await domainsResponse.json()) as any;
    const domain = domainsData['hydra:member']?.[0]?.domain;
    if (!domain) {
      throw new Error('No domains available on MailTM');
    }

    // 2. Generate random email address
    const randomStr = Math.random().toString(36).substring(2, 10);
    this.email = `user_${randomStr}@${domain}`;

    // 3. Create account
    const createAccountResponse = await fetch('https://api.mail.tm/accounts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ address: this.email, password: this.password }),
    });

    if (!createAccountResponse.ok) {
      const errorText = await createAccountResponse.text();
      throw new Error(`Failed to create MailTM account: ${errorText}`);
    }

    // 4. Get token
    const tokenResponse = await fetch('https://api.mail.tm/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ address: this.email, password: this.password }),
    });

    if (!tokenResponse.ok) {
      throw new Error(`Failed to retrieve MailTM token: ${tokenResponse.statusText}`);
    }

    const tokenData = (await tokenResponse.json()) as any;
    this.token = tokenData.token;

    console.log(`MailTM account created successfully: ${this.email}`);
    return this.email;
  }

  /**
   * Poll inbox for a verification email and extract the verification code
   * @param timeoutMs Max time to wait for the email in milliseconds (default 60s)
   */
  async waitForVerificationCode(timeoutMs: number = 60000): Promise<string> {
    const startTime = Date.now();
    console.log(`Polling inbox for ${this.email}...`);

    while (Date.now() - startTime < timeoutMs) {
      const response = await fetch('https://api.mail.tm/messages', {
        headers: {
          Authorization: `Bearer ${this.token}`,
        },
      });

      if (response.ok) {
        const messagesData = (await response.json()) as any;
        const messages = messagesData['hydra:member'] || [];

        for (const message of messages) {
          if (message.subject && message.subject.includes('xác minh')) {
            // Fetch complete message to read body
            const msgDetailsResponse = await fetch(`https://api.mail.tm/messages/${message.id}`, {
              headers: {
                Authorization: `Bearer ${this.token}`,
              },
            });

            if (msgDetailsResponse.ok) {
              const msgDetails = (await msgDetailsResponse.json()) as any;
              const textContent = msgDetails.text || msgDetails.html || '';
              console.log('Verification email content retrieved.');

              // Look for 6-digit verification code or custom formats
              // Verification code is usually 6 digits or words like "mã xác minh của bạn là: 123456"
              const codeMatch = textContent.match(/\b\d{6}\b/);
              if (codeMatch) {
                console.log(`Verification code found: ${codeMatch[0]}`);
                return codeMatch[0];
              }
            }
          }
        }
      }

      // Wait 3 seconds before next poll
      await new Promise((resolve) => setTimeout(resolve, 3000));
    }

    throw new Error(`Timeout waiting for verification email to ${this.email}`);
  }
}
