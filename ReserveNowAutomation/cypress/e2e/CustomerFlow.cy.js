describe('Customer Flow - E2E', () => {
  before(() => {
    cy.log("******    Launch app   *******");
    cy.visit('http://localhost:8081/');
  });

  it('Logs in the customer', () => {
    cy.title().should('eq', 'Home - Restaurant Reservation');

    cy.screenshot("landingPage");
    cy.wait(3000);

    // click login button
    cy.get("button[class='el-button el-button--default login-button']").click();

    // confirm you're on login page
    cy.url().should('include', 'login');

    cy.get("div.login-header").contains("ReserveNow");

    // Load fixture data only AFTER login page is loaded
    cy.fixture('customerLogin').then((loginData) => {
      cy.log("Username: " + loginData.customerUsername);
      cy.log("Password: " + loginData.customerPassword);

      cy.get('input[placeholder="Please input email"]').type(loginData.customerUsername);
      cy.get('input[placeholder="Please input password"]').type(loginData.customerPassword);

      cy.wait(10000);
      cy.get("button[class='el-button el-button--primary el-button--default login-btn']").click();

      // Continue the rest of your test after successful login
      cy.get("div.banner-content").contains("Book");
      cy.get('.logo-title').should('exist');
      cy.get('.logo-icon').should('be.visible');

      cy.get("span[class='user-name']").should('have.text', "user321");
      cy.get("span[class='user-role']")
        .should('have.text', loginData.customerExpect)
        .and('not.have.text', 'Admin');

      cy.get("button[class='el-button el-button--primary el-button--default reserve-btn']").click();
      cy.url().should('include', 'restaurant');

      cy.get("div.restaurant-info-card > div > div").should('have.length', 3);
      cy.get("div[class='table-type-card vip-table']").click();

      cy.get("span.status-badge.available").then(($badge) => {
        if ($badge.length > 0) {
          cy.get("button.select-btn").click();
        }
      });

      cy.get('.time-grid button').first().click();

      cy.get("button[class='el-button el-button--primary el-button--default confirm-btn']").click();

    });
  });

  after(() => {
    cy.log("*******   Close app   *******");
    cy.clearCookies();
    cy.clearLocalStorage();
  });
});
