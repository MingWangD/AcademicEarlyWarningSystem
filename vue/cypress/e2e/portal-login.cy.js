describe('Portal login and role navigation', () => {
  it('renders login page and supports role selection', () => {
    cy.visit('/login')
    cy.contains('学业预警系统登录')
    cy.get('input[placeholder="请输入账号"]').type('teacher01')
    cy.get('input[placeholder="请输入密码"]').type('123456')
    cy.get('.el-select').click()
    cy.contains('教师端').click()
    cy.intercept('POST', '**/api/v1/auth/login', {
      statusCode: 200,
      body: { code: 0, message: '成功', data: { token: 'mock-jwt', userId: 2, role: 'TEACHER' } }
    }).as('login')
    cy.contains('登 录').click()
    cy.wait('@login')
    cy.url().should('include', '/portal/teacher/publish')
  })

  it('rejects route access for mismatched role', () => {
    window.localStorage.setItem('system-user', JSON.stringify({ token: 'mock', role: 'STUDENT' }))
    cy.visit('/portal/admin/users')
    cy.url().should('include', '/login')
  })
})
