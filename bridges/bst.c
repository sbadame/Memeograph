#include <stdio.h>
#include <stdlib.h>

struct bst {
        int data;

        struct bst* left;
        struct bst* right;
};

struct bst* insert(int d, struct bst* root)
{
        struct bst* curr = root;
        struct bst* nn = (struct bst*)malloc(sizeof(struct bst));
        nn->left = NULL;
        nn->right = NULL;
        nn->data = d;
        if (root == NULL) {
                return nn;
        }

        for (;;) {
                if (d <= curr->data) {
                        if (curr->left != NULL) {
                                curr = curr->left;
                        } else {
                                curr->left = nn;
                                return root;
                        }
                } else {
                        if (curr->right != NULL) {
                                curr = curr->right;
                        } else {
                                curr->right = nn;
                                return root;
                        }
                }
        }

        return root;
}

int main()
{
        int i;
        struct bst* tree = NULL;
        for (i = 0; i < 15; i++) {
                tree = insert(rand(), tree);
        }

        return 0;
}
