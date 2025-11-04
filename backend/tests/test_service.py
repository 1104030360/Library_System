#!/usr/bin/env python3
"""Test script for Ollama service - simple and direct"""

import requests
import json

BASE_URL = "http://localhost:8000"


def test_health():
    """Test health check"""
    print("Testing health check...")
    r = requests.get(f"{BASE_URL}/health")
    assert r.status_code == 200
    data = r.json()
    assert data['status'] == 'healthy'
    print("✓ Health check passed")


def test_personal_recommendations():
    """Test personal recommendations"""
    print("\nTesting personal recommendations...")

    payload = {
        "user_profile": {
            "borrow_history": [
                {"title": "深入淺出 Java", "rating": 5},
                {"title": "Python 入門", "rating": 4}
            ]
        },
        "available_books": [
            {"id": "B001", "title": "設計模式", "author": "GoF", "publisher": "碁峰"},
            {"id": "B002", "title": "Clean Code", "author": "Robert Martin", "publisher": "博碩"},
            {"id": "B003", "title": "演算法圖解", "author": "Aditya", "publisher": "碁峰"},
            {"id": "B004", "title": "商業模式新生代", "author": "Alexander", "publisher": "早安財經"},
            {"id": "B005", "title": "原子習慣", "author": "James Clear", "publisher": "方智"}
        ]
    }

    r = requests.post(
        f"{BASE_URL}/generate-personal-recommendations",
        json=payload,
        timeout=60  # Increased for LLM generation
    )

    assert r.status_code == 200
    data = r.json()
    assert data['success'] == True
    assert len(data['recommendations']) > 0

    for rec in data['recommendations']:
        print(f"  - {rec['book_id']}: {rec['reason'][:50]}...")

    print("✓ Personal recommendations passed")


def test_related_recommendations():
    """Test related recommendations"""
    print("\nTesting related recommendations...")

    payload = {
        "current_book": {
            "id": "B001",
            "title": "深入淺出 Java",
            "author": "Kathy Sierra"
        },
        "related_books": [
            {"id": "B002", "title": "Effective Java", "author": "Joshua Bloch"},
            {"id": "B003", "title": "Java 並行程式設計", "author": "Brian Goetz"},
            {"id": "B004", "title": "Spring 實戰", "author": "Craig Walls"}
        ]
    }

    r = requests.post(
        f"{BASE_URL}/generate-related-recommendations",
        json=payload,
        timeout=60  # Increased for LLM generation
    )

    assert r.status_code == 200
    data = r.json()
    assert data['success'] == True

    print("✓ Related recommendations passed")


if __name__ == '__main__':
    print("Starting Ollama service tests...\n")
    try:
        test_health()
        test_personal_recommendations()
        test_related_recommendations()
        print("\n✓ All tests passed!")
    except Exception as e:
        print(f"\n✗ Test failed: {e}")
        exit(1)
