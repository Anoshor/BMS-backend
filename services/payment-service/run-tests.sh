#!/bin/bash

# Payment Service Integration Test Runner
# This script runs all integration tests and reports results

echo "🧪 Payment Service Integration Tests"
echo "===================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven not found. Please install Maven first.${NC}"
    exit 1
fi

echo -e "${YELLOW}📋 Prerequisites Check:${NC}"
echo ""

# Check if core-service is running
echo -n "Checking core-service (port 8080)... "
if curl -s http://localhost:8080/actuator/health &> /dev/null || curl -s http://localhost:8080 &> /dev/null; then
    echo -e "${GREEN}✅ Running${NC}"
    CORE_RUNNING=true
else
    echo -e "${YELLOW}⚠️  Not running (some tests will be skipped)${NC}"
    CORE_RUNNING=false
fi

# Check if payment-service is running
echo -n "Checking payment-service (port 8082)... "
if curl -s http://localhost:8082/actuator/health &> /dev/null || curl -s http://localhost:8082 &> /dev/null; then
    echo -e "${YELLOW}⚠️  Already running (will be stopped for tests)${NC}"
    echo "   Tests will start a new instance automatically"
fi

echo ""
echo -e "${YELLOW}🧪 Running Integration Tests...${NC}"
echo ""

# Run tests
cd "$(dirname "$0")"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn test -Dtest=PaymentIntegrationTest,SecureLeasePaymentTest -Dspring.profiles.active=test

TEST_EXIT_CODE=$?

echo ""
echo "===================================="

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Start both services:"
    echo "   - Core service: cd services/core-service && mvn spring-boot:run"
    echo "   - Payment service: cd services/payment-service && mvn spring-boot:run"
    echo "2. Test with cURL (see TEST_GUIDE.md)"
    echo "3. Integrate with frontend"
else
    echo -e "${RED}❌ Some tests failed!${NC}"
    echo ""
    echo "Troubleshooting:"
    echo "1. Check if Stripe API keys are set in application.yml"
    echo "2. Make sure no other service is running on ports 8080 or 8082"
    echo "3. Check logs above for specific errors"
    echo "4. See TEST_GUIDE.md for detailed troubleshooting"
fi

echo ""

exit $TEST_EXIT_CODE
